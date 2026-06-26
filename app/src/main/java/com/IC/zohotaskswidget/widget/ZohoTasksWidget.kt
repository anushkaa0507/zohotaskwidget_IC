package com.IC.zohotaskswidget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.IC.zohotaskswidget.MainActivity

class ZohoTasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val prefs = context.getSharedPreferences("widget_cache", Context.MODE_PRIVATE)
        val taskCount = prefs.getInt("task_count", 0)
        val pendingCount = prefs.getInt("pending_count", 0)
        val highCount = prefs.getInt("high_count", 0)

        data class CachedTask(val subject: String, val priority: String, val dueDate: String?)

        val tasks = (0 until minOf(taskCount, 3)).map { i ->
            CachedTask(
                subject = prefs.getString("task_${i}_subject", "Task") ?: "Task",
                priority = prefs.getString("task_${i}_priority", "Normal") ?: "Normal",
                dueDate = prefs.getString("task_${i}_due", null)
            )
        }

        provideContent {
            WidgetRoot(pendingCount, highCount, tasks.map { Triple(it.subject, it.priority, it.dueDate) })
        }
    }

    @Composable
    private fun WidgetRoot(
        pendingCount: Int,
        highCount: Int,
        tasks: List<Triple<String, String, String?>>
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>())
        ) {
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                SummaryCard(
                    modifier = GlanceModifier.defaultWeight().wrapContentHeight(),
                    bgColor = Color(0xFF6D9E4F),
                    topLabel = "Pending",
                    bigNumber = "$pendingCount",
                    subLabel = "tasks"
                )
                Spacer(GlanceModifier.width(8.dp))
                SummaryCard(
                    modifier = GlanceModifier.defaultWeight().wrapContentHeight(),
                    bgColor = Color(0xFFE07A30),
                    topLabel = "High Priority",
                    bigNumber = "$highCount",
                    subLabel = "urgent"
                )
                Spacer(GlanceModifier.width(8.dp))
                SummaryCard(
                    modifier = GlanceModifier.defaultWeight().wrapContentHeight(),
                    bgColor = Color(0xFF7B68EE),
                    topLabel = "Total",
                    bigNumber = "${tasks.size}",
                    subLabel = "loaded"
                )
            }

            Spacer(GlanceModifier.height(8.dp))

            tasks.forEach { (subject, priority, dueDate) ->
                TaskRow(subject, priority, dueDate)
                Spacer(GlanceModifier.height(5.dp))
            }

            if (tasks.isEmpty()) {
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(Color(0xFF2A2A2A))
                        .cornerRadius(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks — tap to refresh",
                        style = TextStyle(
                            color = ColorProvider(Color.White.copy(alpha = 0.6f)),
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }

    @Composable
    private fun SummaryCard(
        modifier: GlanceModifier,
        bgColor: Color,
        topLabel: String,
        bigNumber: String,
        subLabel: String
    ) {
        Column(
            modifier = modifier
                .background(bgColor)
                .cornerRadius(16.dp)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = topLabel,
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.85f)),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = bigNumber,
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            )
            Text(
                text = subLabel,
                style = TextStyle(
                    color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                    fontSize = 10.sp
                )
            )
        }
    }

    @Composable
    private fun TaskRow(subject: String, priority: String, dueDate: String?) {
        val (cardColor, accentColor) = when (priority.lowercase()) {
            "high" -> Color(0xFFB84040) to Color(0xFFFF6B6B)
            "medium" -> Color(0xFFB07020) to Color(0xFFFFB347)
            "low" -> Color(0xFF3A7A3A) to Color(0xFF66CC66)
            else -> Color(0xFF2C4A70) to Color(0xFF5599DD)
        }

        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(cardColor)
                .cornerRadius(12.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .width(4.dp)
                    .height(32.dp)
                    .background(accentColor)
                    .cornerRadius(2.dp)
            ) {}
            Spacer(GlanceModifier.width(10.dp))
            Column(modifier = GlanceModifier.defaultWeight()) {
                Text(
                    text = subject,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 1
                )
                if (dueDate != null) {
                    Spacer(GlanceModifier.height(2.dp))
                    Text(
                        text = "Due $dueDate",
                        style = TextStyle(
                            color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                            fontSize = 10.sp
                        )
                    )
                }
            }
            Spacer(GlanceModifier.width(8.dp))
            Text(
                text = priority.uppercase().take(3),
                style = TextStyle(
                    color = ColorProvider(accentColor),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}