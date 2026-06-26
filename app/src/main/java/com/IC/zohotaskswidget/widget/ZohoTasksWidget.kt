package com.IC.zohotaskswidget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.background
import androidx.compose.ui.graphics.Color

class ZohoTasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            ZohoTasksWidgetContent()
        }
    }

    @Composable
    private fun ZohoTasksWidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = "Zoho Tasks",
                style = TextStyle(
                    fontSize = androidx.compose.ui.unit.TextUnit(18f, androidx.compose.ui.unit.TextUnitType.Sp),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            GlowingTaskWidgetCard(
                subject = "Task 1",
                priority = "High",
                status = "Pending"
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            GlowingTaskWidgetCard(
                subject = "Task 2",
                priority = "Medium",
                status = "In Progress"
            )

            Spacer(modifier = GlanceModifier.height(8.dp))

            GlowingTaskWidgetCard(
                subject = "Task 3",
                priority = "Low",
                status = "Pending"
            )
        }
    }

    @Composable
    private fun GlowingTaskWidgetCard(
        subject: String,
        priority: String,
        status: String
    ) {
        val priorityColor = when (priority.lowercase()) {
            "high" -> Color(0xFFFF6B6B)
            "medium" -> Color(0xFFFFA502)
            "low" -> Color(0xFF51CF66)
            else -> Color(0xFF4A90E2)
        }

        Column(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E1E))
                .padding(8.dp)
        ) {
            Row(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(start = 8.dp)
                ) {
                    Text(
                        text = subject,
                        style = TextStyle(
                            fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp),
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Text(
                            text = priority.uppercase(),
                            style = TextStyle(
                                fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp)
                            )
                        )

                        Spacer(modifier = GlanceModifier.width(8.dp))

                        Text(
                            text = status,
                            style = TextStyle(
                                fontSize = androidx.compose.ui.unit.TextUnit(10f, androidx.compose.ui.unit.TextUnitType.Sp)
                            )
                        )
                    }
                }
            }
        }
    }
}