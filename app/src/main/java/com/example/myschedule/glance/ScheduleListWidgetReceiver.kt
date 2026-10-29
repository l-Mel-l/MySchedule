package com.example.myschedule.glance

import androidx.glance.appwidget.GlanceAppWidget

class ScheduleListWidgetReceiver : RefreshingWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ScheduleListWidget()
}
