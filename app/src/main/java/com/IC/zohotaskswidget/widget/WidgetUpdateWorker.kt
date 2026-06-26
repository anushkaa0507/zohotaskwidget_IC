package com.IC.zohotaskswidget.widget

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.IC.zohotaskswidget.api.RetrofitClient
import com.IC.zohotaskswidget.api.TaskData
import com.IC.zohotaskswidget.auth.TokenManager
import com.IC.zohotaskswidget.repository.AuthRepository
import com.IC.zohotaskswidget.repository.TaskRepository
import java.util.concurrent.TimeUnit

class WidgetUpdateWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val tokenManager = TokenManager(context)
            if (!tokenManager.isAuthenticated()) return Result.success()

            RetrofitClient.initialize(context)
            val authRepository = AuthRepository(tokenManager)
            val taskRepository = TaskRepository(tokenManager = tokenManager, authRepository = authRepository)

            val result = taskRepository.getPendingTasks()
            result.onSuccess { tasks ->
                cacheTasks(tasks)
                refreshWidget()
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun cacheTasks(tasks: List<TaskData>) {
        val prefs = context.getSharedPreferences("widget_cache", Context.MODE_PRIVATE)
        val pending = tasks.count { it.status?.lowercase()?.contains("complet") == false }
        val high = tasks.count { it.priority?.lowercase() == "high" }

        prefs.edit().apply {
            putInt("task_count", tasks.size)
            putInt("pending_count", pending)
            putInt("high_count", high)
            tasks.take(3).forEachIndexed { i, task ->
                putString("task_${i}_subject", task.subject)
                putString("task_${i}_priority", task.priority ?: "Normal")
                putString("task_${i}_due", task.dueDate)
            }
        }.apply()
    }

    private suspend fun refreshWidget() {
        val manager = androidx.glance.appwidget.GlanceAppWidgetManager(context)
        val widget = ZohoTasksWidget()
        manager.getGlanceIds(ZohoTasksWidget::class.java).forEach { id ->
            widget.update(context, id)
        }
    }

    companion object {
        private const val WORK_NAME = "zoho_widget_refresh"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun scheduleImmediate(context: Context) {
            val request = OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }
    }
}