package com.example.myschedule.glance

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

// Это "дверь", через которую система общается с твоим виджетом
class ScheduleWidgetReceiver : GlanceAppWidgetReceiver() {

    // Мы говорим системе: "Внешний вид этого виджета лежит в классе ScheduleWidget"
    override val glanceAppWidget: GlanceAppWidget = ScheduleWidget()
}