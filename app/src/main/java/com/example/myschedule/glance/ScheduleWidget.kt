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
import com.example.myschedule.*

class ScheduleWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = ScheduleRepository(context)
        val schedule = repository.loadSchedule()

        // --- 1. ОПРЕДЕЛЯЕМ СОСТОЯНИЕ (Копируем логику из приложения) ---
        val now = java.time.LocalDateTime.now()
        val currentTime = now.toLocalTime()
        val currentDayIndex = now.dayOfWeek.value - 1

        var widgetState: WidgetState = WidgetState.FreeDay // По умолчанию

        if (schedule != null) {
            val settings = schedule.settings

            // Вычисляем неделю
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
                    // Для семестра берем текущую реальную
                    val start = settings.semesterStartDate
                    if (start != null) TimeUtils.getCurrentWeekNumber(start) else 1
                }
            }

            val currentWeek = schedule.weeks.find { it.weekNumber == weekNum }
            val currentDay = currentWeek?.days?.getOrNull(currentDayIndex)

            if (currentDay != null && currentDay.lessons.isNotEmpty()) {
                val lessons = currentDay.lessons.sortedBy { it.startTime }
                widgetState = WidgetState.DayFinished // Если не найдем совпадений

                for ((index, lesson) in lessons.withIndex()) {
                    val start = TimeUtils.parse(lesson.startTime)
                    val end = TimeUtils.parse(lesson.endTime)

                    if (currentTime.isAfter(start) && currentTime.isBefore(end)) {
                        widgetState = WidgetState.LessonNow(lesson)
                        break
                    }
                    if (currentTime.isBefore(start)) {
                        widgetState = WidgetState.BreakNow(lesson) // Это следующая пара
                        break
                    }
                }
            }
        }

        provideContent {
            WidgetContent(widgetState)
        }
    }

    @Composable
    fun WidgetContent(state: WidgetState) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFF121212)))
                .padding(12.dp), // Чуть меньше отступ
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is WidgetState.LessonNow -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ИДЕТ ЗАНЯТИЕ",
                            style = TextStyle(color = ColorProvider(Color(0xFFFFA500)), fontSize = 10.sp) // Мелкий заголовок
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = state.lesson.name,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 16.sp, // Чуть меньше (было 18)
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.glance.text.TextAlign.Center
                            ),
                            maxLines = 2 // Разрешаем 2 строки
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "до ${state.lesson.endTime} • ${state.lesson.room}",
                            style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 12.sp)
                        )
                    }
                }
                is WidgetState.BreakNow -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "СЛЕДУЮЩАЯ ПАРА",
                            style = TextStyle(color = ColorProvider(Color(0xFF66BB6A)), fontSize = 10.sp)
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = state.nextLesson.name,
                            style = TextStyle(
                                color = ColorProvider(Color.White),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.glance.text.TextAlign.Center
                            ),
                            maxLines = 2
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "Начало в ${state.nextLesson.startTime}",
                            style = TextStyle(color = ColorProvider(Color.LightGray), fontSize = 12.sp)
                        )
                    }
                }
                is WidgetState.DayFinished -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ЗАНЯТИЯ ОКОНЧЕНЫ",
                            style = TextStyle(color = ColorProvider(Color.Gray), fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "На сегодня всё!",
                            style = TextStyle(color = ColorProvider(Color.DarkGray), fontSize = 12.sp)
                        )
                    }
                }
                is WidgetState.FreeDay -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ВЫХОДНОЙ",
                            style = TextStyle(color = ColorProvider(Color(0xFFFFA500)), fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        Text(
                            text = "Отдыхайте",
                            style = TextStyle(color = ColorProvider(Color.Gray), fontSize = 12.sp)
                        )
                    }
                }
            }
        }
    }
}

// Вспомогательный класс состояний (только для виджета)
sealed class WidgetState {
    data class LessonNow(val lesson: Lesson) : WidgetState()
    data class BreakNow(val nextLesson: Lesson) : WidgetState()
    object DayFinished : WidgetState()
    object FreeDay : WidgetState()
}