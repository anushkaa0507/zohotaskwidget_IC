package com.IC.zohotaskswidget.repository

import android.util.Log
import com.IC.zohotaskswidget.api.CreateTaskData
import com.IC.zohotaskswidget.api.CreateTaskRequest
import com.IC.zohotaskswidget.api.RetrofitClient
import com.IC.zohotaskswidget.api.TaskData
import com.IC.zohotaskswidget.api.ZohoApiService
import com.IC.zohotaskswidget.auth.TokenManager

class TaskRepository(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) {
    private val apiService: ZohoApiService = RetrofitClient.getCRMService()

    suspend fun getPendingTasks(): Result<List<TaskData>> = try {
        val token = tokenManager.getAccessToken() ?: return Result.failure(
            Exception("No access token available")
        )

        val response = apiService.getTasks(
            authorization = "Zoho-oauthtoken $token",
            fields = "Subject,Status,Due_Date,Priority",
            page = 1,
            perPage = 20
        )

        if (response.isSuccessful) {
            val tasks = response.body()?.data?.filter { task: TaskData ->
                task.status?.lowercase() != "completed"
            } ?: emptyList()
            Result.success(tasks)
        } else {
            Result.failure(Exception("Failed to load tasks: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e("TaskRepository", "Error getting pending tasks", e)
        Result.failure(e)
    }

    suspend fun getCompletedTasks(): Result<List<TaskData>> = try {
        val token = tokenManager.getAccessToken() ?: return Result.failure(
            Exception("No access token available")
        )

        val response = apiService.getTasks(
            authorization = "Zoho-oauthtoken $token"
        )

        if (response.isSuccessful) {
            val tasks = response.body()?.data?.filter { task: TaskData ->
                task.status?.lowercase() == "completed"
            } ?: emptyList()
            Result.success(tasks)
        } else {
            Result.failure(Exception("Failed to load tasks: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e("TaskRepository", "Error getting completed tasks", e)
        Result.failure(e)
    }

    suspend fun getOverdueTasks(): Result<List<TaskData>> = try {
        val token = tokenManager.getAccessToken() ?: return Result.failure(
            Exception("No access token available")
        )

        val response = apiService.getTasks(
            authorization = "Zoho-oauthtoken $token"
        )

        if (response.isSuccessful) {
            val tasks = response.body()?.data?.filter { task: TaskData ->
                task.dueDate != null &&
                        task.status?.lowercase() != "completed"
            } ?: emptyList()
            Result.success(tasks)
        } else {
            Result.failure(Exception("Failed to load tasks: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e("TaskRepository", "Error getting overdue tasks", e)
        Result.failure(e)
    }

    suspend fun getHighPriorityTasks(): Result<List<TaskData>> = try {
        val token = tokenManager.getAccessToken() ?: return Result.failure(
            Exception("No access token available")
        )

        val response = apiService.getTasks(
            authorization = "Zoho-oauthtoken $token"
        )

        if (response.isSuccessful) {
            val tasks = response.body()?.data?.filter { task: TaskData ->
                task.priority?.lowercase() == "high"
            } ?: emptyList()
            Result.success(tasks)
        } else {
            Result.failure(Exception("Failed to load tasks: ${response.code()}"))
        }
    } catch (e: Exception) {
        Log.e("TaskRepository", "Error getting high priority tasks", e)
        Result.failure(e)
    }

    suspend fun createTask(
        subject: String,
        priority: String? = null,
        dueDate: String? = null
    ): Result<List<TaskData>> = try {
        val token = tokenManager.getAccessToken() ?: return Result.failure(
            Exception("No access token available")
        )

        val taskData = CreateTaskData(
            subject = subject,
            priority = priority,
            dueDate = dueDate,
            status = "Not Started"
        )

        val request = CreateTaskRequest(data = listOf(taskData))

        val response = apiService.createTask(
            authorization = "Zoho-oauthtoken $token",
            taskData = request
        )

        if (response.isSuccessful) {
            Log.d("TaskRepository", "Task created successfully")
            Result.success(response.body()?.data ?: emptyList())
        } else {
            Log.e("TaskRepository", "Failed to create task: ${response.code()}")
            Result.failure(Exception("Failed to create task: ${response.errorBody()?.string()}"))
        }
    } catch (e: Exception) {
        Log.e("TaskRepository", "Error creating task", e)
        Result.failure(e)
    }
}