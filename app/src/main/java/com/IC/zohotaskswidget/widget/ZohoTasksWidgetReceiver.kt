package com.IC.zohotaskswidget.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class ZohoTasksWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: ZohoTasksWidget = ZohoTasksWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        WidgetUpdateWorker.scheduleImmediate(context)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.schedule(context)
        WidgetUpdateWorker.scheduleImmediate(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
    }
}