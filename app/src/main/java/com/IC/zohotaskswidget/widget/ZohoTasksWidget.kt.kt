package com.IC.zohotaskswidget.widget

import android.content.Context
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceId
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text

class `ZohoTasksWidget.kt` : GlanceAppWidget() {

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId
    ) {
        provideContent {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                Text("Zoho Tasks")
                Text("Widget Working")
            }
        }
    }
}