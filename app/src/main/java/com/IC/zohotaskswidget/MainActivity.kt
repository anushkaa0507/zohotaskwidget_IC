// File: app/src/main/java/com/IC/zohotaskswidget/MainActivity.kt

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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.IC.zohotaskswidget.api.RetrofitClient
import com.IC.zohotaskswidget.api.TaskData
import com.IC.zohotaskswidget.auth.TokenManager
import com.IC.zohotaskswidget.repository.AuthRepository
import com.IC.zohotaskswidget.repository.TaskRepository
import com.IC.zohotaskswidget.ui.theme.ZohoTasksWidgetTheme
import com.IC.zohotaskswidget.utils.Constants
import kotlinx.coroutines.launch

/**
 * Main activity for the Zoho Tasks Widget application.
 * Handles OAuth login flow, task display, and task management.
 */
class MainActivity : ComponentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var tokenManager: TokenManager
    private lateinit var authRepository: AuthRepository
    private lateinit var taskRepository: TaskRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize token manager and repositories
        tokenManager = TokenManager(this)
        RetrofitClient.initialize(this)
        authRepository = AuthRepository(tokenManager)
        taskRepository = TaskRepository(tokenManager, authRepository)

        // Handle OAuth callback if app was opened by Zoho
        handleOAuthIntent(intent)

        enableEdgeToEdge()

        setContent {
            ZohoTasksWidgetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (authRepository.isAuthenticated()) {
                        TasksScreen(taskRepository, authRepository)
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

    /**
     * Handle OAuth callback from Zoho.
     * Extracts authorization code and exchanges it for access token.
     */
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
                    // Valid authorization code received
                    Log.d(TAG, "Authorization code received, exchanging for access token...")
                    exchangeCodeForToken(authCode)
                }
                error != null -> {
                    // OAuth error occurred
                    Log.e(TAG, "OAuth error: $error - $errorDescription")
                    showError("Authentication failed: $errorDescription")
                }
                else -> {
                    Log.d(TAG, "No authorization code or error in URI")
                }
            }
        }
    }

    /**
     * Exchange authorization code for access token.
     */
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

                    // Refresh UI to show tasks screen
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

    /**
     * Open Zoho OAuth login page in browser.
     */
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

    /**
     * Build complete OAuth URL with all required parameters.
     */
    private fun buildOAuthUrl(): String {
        return "${Constants.ACCOUNTS_BASE_URL}/oauth/v2/auth" +
                "?scope=${Constants.OAUTH_SCOPE}" +
                "&client_id=${Constants.CLIENT_ID}" +
                "&response_type=code" +
                "&access_type=offline" +
                "&redirect_uri=${Constants.REDIRECT_URI}"
    }

    /**
     * Show error message to user.
     */
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Show success message to user.
     */
    private fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Login screen UI component.
 */
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

/**
 * Tasks display screen UI component.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(taskRepository: TaskRepository, authRepository: AuthRepository) {
    var tasks by remember { mutableStateOf<List<TaskData>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("pending") }

    // Load tasks on composition
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
                        // Refresh tasks
                        isLoading = true
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter buttons
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

            // Tasks list or loading/error state
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
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(tasks) { task ->
                            TaskCard(task)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Filter button UI component.
 */
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

/**
 * Task card UI component.
 */
@Composable
fun TaskCard(task: TaskData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = task.subject ?: "Untitled",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (task.priority != null) {
                    Label("Priority: ${task.priority}")
                }
                if (task.status != null) {
                    Label("Status: ${task.status}")
                }
            }

            if (task.dueDate != null) {
                Text(
                    text = "Due: ${task.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

/**
 * Label UI component.
 */
@Composable
fun Label(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(4.dp, 2.dp)
        )
    }
}