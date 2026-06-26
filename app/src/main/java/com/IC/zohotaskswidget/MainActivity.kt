package com.IC.zohotaskswidget

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.text.style.TextAlign
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.IC.zohotaskswidget.api.RetrofitClient
import com.IC.zohotaskswidget.api.TaskData
import com.IC.zohotaskswidget.auth.TokenManager
import com.IC.zohotaskswidget.repository.AuthRepository
import com.IC.zohotaskswidget.repository.TaskRepository
import com.IC.zohotaskswidget.ui.theme.ZohoTasksWidgetTheme
import com.IC.zohotaskswidget.utils.Constants
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository
    private lateinit var taskRepository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tokenManager = TokenManager(this)
        RetrofitClient.initialize(this)
        authRepository = AuthRepository(tokenManager)
        taskRepository = TaskRepository(tokenManager = tokenManager, authRepository = authRepository)

        handleOAuthIntent(intent)

        enableEdgeToEdge()

        setContent {
            ZohoTasksWidgetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (authRepository.isAuthenticated()) {
                        TasksScreen(
                            taskRepository = taskRepository,
                            authRepository = authRepository,
                            onShowError = { showError(it) },
                            onShowSuccess = { showSuccess(it) }
                        )
                    } else {
                        LoginScreen { openZohoLogin() }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthIntent(intent)
    }

    private fun handleOAuthIntent(intent: Intent?) {
        val uri = intent?.data
        Log.d(TAG, "URI = $uri")

        uri?.let {
            val authCode = it.getQueryParameter("code")
            val error = it.getQueryParameter("error")
            val errorDescription = it.getQueryParameter("error_description")

            Log.d(TAG, "CODE = $authCode")

            when {
                authCode != null -> {
                    Log.d(TAG, "Authorization code received, exchanging for access token...")
                    exchangeCodeForToken(authCode)
                }
                error != null -> {
                    Log.e(TAG, "OAuth error: $error - $errorDescription")
                    showError("Authentication failed: $errorDescription")
                }
                else -> {
                    Log.d(TAG, "No authorization code or error in URI")
                }
            }
        }
    }

    private fun exchangeCodeForToken(authCode: String) {
        lifecycleScope.launch {
            try {
                Log.d(TAG, "Attempting to exchange auth code for access token")

                val result = authRepository.getAccessToken(authCode)

                result.onSuccess { tokenResponse ->
                    Log.d(TAG, "Successfully obtained access token")
                    Log.d(TAG, "Token expires in: ${tokenResponse.expiresIn} seconds")
                    Log.d(TAG, "API Domain: ${tokenResponse.apiDomain}")

                    showSuccess("Successfully logged in!")
                    recreate()
                }

                result.onFailure { exception ->
                    Log.e(TAG, "Failed to exchange auth code", exception)
                    showError("Failed to login: ${exception.message}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while exchanging auth code", e)
                showError("Login error: ${e.message}")
            }
        }
    }

    private fun openZohoLogin() {
        try {
            val url = buildOAuthUrl()
            Log.d(TAG, "Opening Zoho login URL: $url")

            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url))
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Zoho login", e)
            showError("Failed to open login page: ${e.message}")
        }
    }

    private fun buildOAuthUrl(): String {
        return "${Constants.ACCOUNTS_BASE_URL}/oauth/v2/auth" +
                "?scope=${Constants.OAUTH_SCOPE}" +
                "&client_id=${Constants.CLIENT_ID}" +
                "&response_type=code" +
                "&access_type=offline" +
                "&redirect_uri=${Constants.REDIRECT_URI}"
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun LoginScreen(onLoginClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Zoho CRM Tasks Widget",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Stay organized with your Zoho CRM tasks right on your home screen",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 32.dp),
            textAlign = TextAlign.Center
        )

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Login with Zoho", style = MaterialTheme.typography.titleMedium)
        }

        Text(
            text = "⚠️ Note: Zoho free plan may not support third-party integrations.\nConsider upgrading to a paid plan for full functionality.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    taskRepository: TaskRepository,
    authRepository: AuthRepository,
    onShowError: (String) -> Unit,
    onShowSuccess: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var tasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("pending") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedFilter) {
        isLoading = true
        error = null
        try {
            val result = when (selectedFilter) {
                "completed" -> taskRepository.getCompletedTasks()
                "overdue" -> taskRepository.getOverdueTasks()
                "high-priority" -> taskRepository.getHighPriorityTasks()
                else -> taskRepository.getPendingTasks()
            }

            result.onSuccess { taskList ->
                tasks = taskList
            }
            result.onFailure { exception ->
                error = exception.message
                Log.e("TasksScreen", "Failed to load tasks", exception)
            }
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Zoho Tasks") },
                actions = {
                    IconButton(onClick = {
                        isLoading = true
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FilterButton("Pending", selectedFilter == "pending") {
                    selectedFilter = "pending"
                }
                FilterButton("Completed", selectedFilter == "completed") {
                    selectedFilter = "completed"
                }
                FilterButton("Overdue", selectedFilter == "overdue") {
                    selectedFilter = "overdue"
                }
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Error loading tasks",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                error ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                tasks.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No tasks to display")
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(tasks) { task ->
                            GlowingTaskCard(task)
                        }
                    }
                }
            }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onAddTask = { subject, priority, dueDate ->
                coroutineScope.launch {
                    try {
                        val result = taskRepository.createTask(subject, priority, dueDate)
                        result.onSuccess {
                            onShowSuccess("Task created!")
                            showAddTaskDialog = false
                        }
                        result.onFailure { exception ->
                            onShowError("Failed to create task: ${exception.message}")
                        }
                    } catch (e: Exception) {
                        onShowError("Error: ${e.message}")
                    }
                }
            }
        )
    }
}

@Composable
fun FilterButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.secondary
        )
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun GlowingTaskCard(task: TaskData) {
    val priorityColor = when (task.priority?.lowercase()) {
        "high" -> Color(0xFFFF6B6B)
        "medium" -> Color(0xFFFFA502)
        "low" -> Color(0xFF51CF66)
        else -> Color(0xFF4A90E2)
    }

    val glowColor = priorityColor.copy(alpha = 0.3f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(12.dp),
                clip = false
            )
            .background(
                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    radius = 200f
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = priorityColor,
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp)
            ) {
                Text(
                    text = task.subject ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (task.priority != null) {
                        PriorityLabel(task.priority, priorityColor)
                    }
                    if (task.status != null) {
                        StatusLabel(task.status)
                    }
                }

                if (task.dueDate != null) {
                    Text(
                        text = "Due: ${task.dueDate}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun PriorityLabel(priority: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.2f),
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = priority.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(6.dp, 4.dp),
            color = color
        )
    }
}

@Composable
fun StatusLabel(status: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = status,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(6.dp, 4.dp),
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onAddTask: (String, String, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("Medium") }
    var dueDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Task Subject") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )

                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("High", "Medium", "Low").forEach { p ->
                        Button(
                            onClick = { priority = p },
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (priority == p)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(p, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = dueDate,
                    onValueChange = { dueDate = it },
                    label = { Text("Due Date (YYYY-MM-DD)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (subject.isNotBlank()) {
                    onAddTask(subject, priority, dueDate)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}