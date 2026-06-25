// File: app/src/main/java/com/IC/zohotaskswidget/repository/TaskRepository.kt

package com.IC.zohotaskswidget.repository

import android.util.Log
import com.IC.zohotaskswidget.api.RetrofitClient
import com.IC.zohotaskswidget.api.TaskData
import com.IC.zohotaskswidget.api.UpdateTaskData
import com.IC.zohotaskswidget.api.UpdateTaskRequest
import com.IC.zohotaskswidget.auth.TokenManager

/**
 * Repository for handling task-related operations.
 * Manages fetching, updating, and filtering tasks from Zoho CRM.
 */
class TaskRepository(
    private val tokenManager: TokenManager,
    private val authRepository: AuthRepository
) {

    companion object {
        private const val TAG = "TaskRepository"
        private const val PAGE_SIZE = 20

        // Task status constants
        const val STATUS_PENDING = "Pending"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_DEFERRED = "Deferred"
    }

    // ========== FETCH TASKS ==========

    /**
     * Fetch all tasks from Zoho CRM.
     *
     * @param page Page number for pagination (starting from 1)
     * @param perPage Number of records per page (max 200)
     * @param sortBy Field to sort by (e.g., "Due_Date")
     * @param sortOrder Sort order: "asc" or "desc"
     * @return Result containing list of tasks or error
     */
    suspend fun getTasks(
        page: Int = 1,
        perPage: Int = PAGE_SIZE,
        sortBy: String = "Due_Date",
        sortOrder: String = "asc"
    ): Result<List<TaskData>> {
        return try {
            // Ensure token is valid
            if (!tokenManager.isAuthenticated()) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            // Refresh token if needed
            if (authRepository.shouldRefreshToken()) {
                Log.d(TAG, "Token expiring soon, refreshing...")
                authRepository.refreshAccessToken()
            }

            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "No access token available")
                return Result.failure(Exception("No access token available"))
            }

            Log.d(TAG, "Fetching tasks from Zoho CRM (page: $page, perPage: $perPage)")
            val response = RetrofitClient.getCRMService().getTasks(
                authorization = "Bearer $accessToken",
                fields = "Subject,Status,Due_Date,Priority",
                page = page,
                perPage = perPage
            )

            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.data ?: emptyList()
                Log.d(TAG, "Successfully fetched ${tasks.size} tasks")
                Result.success(tasks)
            } else {
                val errorBody = response.errorBody()?.string() ?: "Unknown error"

                Log.e(TAG, "===================================")
                Log.e(TAG, "ZOHO ERROR BODY = $errorBody")
                Log.e(TAG, "HTTP CODE = ${response.code()}")
                Log.e(TAG, "===================================")

                Result.failure(
                    Exception(
                        "Failed to fetch tasks: ${response.code()} - $errorBody"
                    )
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching tasks", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch pending tasks only.
     *
     * @return Result containing list of pending tasks
     */
    suspend fun getPendingTasks(): Result<List<TaskData>> {
        return try {
            Log.d(TAG, "Fetching pending tasks")

            getTasks().mapCatching { tasks ->
                tasks.filter { it.status != STATUS_COMPLETED }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching pending tasks", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch completed tasks only.
     *
     * @return Result containing list of completed tasks
     */
    suspend fun getCompletedTasks(): Result<List<TaskData>> {
        return try {
            Log.d(TAG, "Fetching completed tasks")

            getTasks().mapCatching { tasks ->
                tasks.filter { it.status == STATUS_COMPLETED }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching completed tasks", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch overdue tasks (due date is in the past and not completed).
     *
     * @return Result containing list of overdue tasks
     */
    suspend fun getOverdueTasks(): Result<List<TaskData>> {
        return try {
            Log.d(TAG, "Fetching overdue tasks")

            getTasks().mapCatching { tasks ->
                val now = System.currentTimeMillis()
                tasks.filter { task ->
                    task.status != STATUS_COMPLETED &&
                            task.dueDate != null &&
                            parseDate(task.dueDate!!) < now
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching overdue tasks", e)
            Result.failure(e)
        }
    }

    /**
     * Fetch high priority tasks.
     *
     * @return Result containing list of high priority tasks
     */
    suspend fun getHighPriorityTasks(): Result<List<TaskData>> {
        return try {
            Log.d(TAG, "Fetching high priority tasks")

            getTasks().mapCatching { tasks ->
                tasks.filter {
                    it.priority?.equals("High", ignoreCase = true) == true &&
                            it.status != STATUS_COMPLETED
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching high priority tasks", e)
            Result.failure(e)
        }
    }

    // ========== FETCH SINGLE TASK ==========

    /**
     * Fetch details of a single task.
     *
     * @param taskId Task ID
     * @return Result containing task details
     */
    suspend fun getTaskDetails(taskId: String): Result<TaskData?> {
        return try {
            // Ensure token is valid
            if (!tokenManager.isAuthenticated()) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "No access token available")
                return Result.failure(Exception("No access token available"))
            }

            Log.d(TAG, "Fetching details for task: $taskId")

            val response = RetrofitClient.getCRMService().getTaskDetails(
                taskId = taskId,
                authorization = "Bearer $accessToken"
            )

            if (response.isSuccessful && response.body() != null) {
                val task = response.body()!!.data?.firstOrNull()
                Log.d(TAG, "Successfully fetched task details")
                Result.success(task)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to fetch task details: HTTP ${response.code()}")
                Log.e(TAG, "Error body: $errorMessage")
                Result.failure(Exception("Failed to fetch task details: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while fetching task details", e)
            Result.failure(e)
        }
    }

    // ========== UPDATE TASKS ==========

    /**
     * Mark task as completed.
     *
     * @param taskId Task ID to mark as completed
     * @return Result with success/failure
     */
    suspend fun markTaskCompleted(taskId: String): Result<Unit> {
        return updateTaskStatus(taskId, STATUS_COMPLETED)
    }

    /**
     * Mark task as pending (reopen completed task).
     *
     * @param taskId Task ID to reopen
     * @return Result with success/failure
     */
    suspend fun reopenTask(taskId: String): Result<Unit> {
        return updateTaskStatus(taskId, STATUS_PENDING)
    }

    /**
     * Update task status to specified value.
     *
     * @param taskId Task ID
     * @param status New status value
     * @return Result with success/failure
     */
    private suspend fun updateTaskStatus(taskId: String, status: String): Result<Unit> {
        return try {
            // Ensure token is valid
            if (!tokenManager.isAuthenticated()) {
                Log.e(TAG, "User not authenticated")
                return Result.failure(Exception("User not authenticated"))
            }

            val accessToken = authRepository.getAccessToken()
            if (accessToken == null) {
                Log.e(TAG, "No access token available")
                return Result.failure(Exception("No access token available"))
            }

            Log.d(TAG, "Updating task $taskId status to $status")

            val updateRequest = UpdateTaskRequest(
                data = listOf(
                    UpdateTaskData(id = taskId, status = status)
                )
            )

            val response = RetrofitClient.getCRMService().updateTask(
                authorization = "Bearer $accessToken",
                updateRequest = updateRequest
            )

            if (response.isSuccessful) {
                Log.d(TAG, "Successfully updated task status")
                Result.success(Unit)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e(TAG, "Failed to update task: HTTP ${response.code()}")
                Log.e(TAG, "Error body: $errorMessage")
                Result.failure(Exception("Failed to update task: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while updating task", e)
            Result.failure(e)
        }
    }

    // ========== UTILITY METHODS ==========

    /**
     * Parse date string in Zoho format to milliseconds.
     * Handles multiple date formats.
     *
     * @param dateString Date string from Zoho (format: YYYY-MM-DD or YYYY-MM-DDTHH:mm:ss)
     * @return Milliseconds since epoch, or current time if parsing fails
     */
    private fun parseDate(dateString: String): Long {
        return try {
            // Try parsing as ISO 8601 format
            if (dateString.contains("T")) {
                // Full timestamp format
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                    .parse(dateString)?.time ?: System.currentTimeMillis()
            } else {
                // Date only format
                java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                    .parse(dateString)?.time ?: System.currentTimeMillis()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse date: $dateString", e)
            System.currentTimeMillis()
        }
    }

    /**
     * Check if task is overdue.
     *
     * @param task Task to check
     * @return true if task is overdue and not completed
     */
    fun isOverdue(task: TaskData): Boolean {
        if (task.status == STATUS_COMPLETED || task.dueDate == null) {
            return false
        }
        return parseDate(task.dueDate) < System.currentTimeMillis()
    }

    /**
     * Check if task is due today.
     *
     * @param task Task to check
     * @return true if task is due today
     */
    fun isDueToday(task: TaskData): Boolean {
        if (task.dueDate == null) return false

        val taskDueTime = parseDate(task.dueDate)
        val today = System.currentTimeMillis()

        val taskCalendar = java.util.Calendar.getInstance().apply {
            timeInMillis = taskDueTime
        }
        val todayCalendar = java.util.Calendar.getInstance().apply {
            timeInMillis = today
        }

        return taskCalendar.get(java.util.Calendar.YEAR) == todayCalendar.get(java.util.Calendar.YEAR) &&
                taskCalendar.get(java.util.Calendar.DAY_OF_YEAR) == todayCalendar.get(java.util.Calendar.DAY_OF_YEAR)
    }
}