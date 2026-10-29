package com.example.myschedule.glance

import androidx.glance.appwidget.GlanceAppWidget

class ScheduleWidgetReceiver : RefreshingWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}
