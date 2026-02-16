package com.example.myschedule.glance

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import com.example.myschedule.ScheduleRepository
import com.example.myschedule.ScheduleType
import com.example.myschedule.TimeUtils

class ScheduleListWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = ScheduleRepository(context)
        val schedule = repository.loadSchedule()

        val now = java.time.LocalDateTime.now()
        val currentDayIndex = now.dayOfWeek.value - 1

        var todaysLessons = emptyList<com.example.myschedule.Lesson>()
        var dayTitle = "Сегодня"

        if (schedule != null) {
            val settings = schedule.settings
            val weekNum = when (settings.scheduleType) {
                ScheduleType.Fixed -> 1
                ScheduleType.Rotation -> {
                    val start = settings.semesterStartDate
                    if (start != null) {
                        val abs = TimeUtils.getCurrentWeekNumber(start)
                        if (abs % 2 != 0) 1 else 2
                    } else 1
                }
                ScheduleType.Semester -> {
                    val start = settings.semesterStartDate
                    if (start != null) TimeUtils.getCurrentWeekNumber(start) else 1
                }
            }

            val currentWeek = schedule.weeks.find { it.weekNumber == weekNum }
            val currentDay = currentWeek?.days?.getOrNull(currentDayIndex)

            if (currentDay != null) {
                todaysLessons = currentDay.lessons.sortedBy { it.startTime }
                dayTitle = "${currentDay.dayName} ($weekNum нед.)"
            }
        }

        provideContent {
            ListWidgetContent(dayTitle, todaysLessons)
        }
    }

    @Composable
    fun ListWidgetContent(title: String, lessons: List<com.example.myschedule.Lesson>) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF121212)))
                .padding(12.dp)
        ) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                // Заголовок
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .background(ColorProvider(Color(0xFF2C2C2C)))
                        .padding(8.dp)
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFFFA500)),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                if (lessons.isEmpty()) {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Пар нет", style = TextStyle(color = ColorProvider(Color.Gray)))
                    }
                } else {
                    // Список
                    LazyColumn {
                        items(lessons) { lesson ->
                            LessonItemRow(lesson)
                            Spacer(modifier = GlanceModifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun LessonItemRow(lesson: com.example.myschedule.Lesson) {
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .background(ColorProvider(Color(0xFF1E1E1E)))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Время
            Column(modifier = GlanceModifier.width(50.dp)) {
                Text(lesson.startTime, style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold))
                Text(lesson.endTime, style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 12.sp))
            }

            Spacer(modifier = GlanceModifier.width(8.dp))

            // Цветная палочка
            Box(
                modifier = GlanceModifier
                    .width(4.dp)
                    .height(30.dp)
                    .background(ColorProvider(Color(lesson.color ?: 0xFFFFA500)))
            ) {}

            Spacer(modifier = GlanceModifier.width(12.dp))

            // Инфо
            Column {
                Text(lesson.name, style = TextStyle(color = ColorProvider(Color.White)), maxLines = 1)
                if (lesson.room.isNotEmpty()) {
                    Text(lesson.room, style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 12.sp))
                }
            }
        }
    }
}