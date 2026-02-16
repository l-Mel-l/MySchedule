package com.example.myschedule.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myschedule.DaySchedule
import com.example.myschedule.Lesson

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DayScheduleView(
    daySchedule: DaySchedule,
    onLessonClick: (Lesson) -> Unit,
    onLessonLongClick: (Lesson) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = daySchedule.dayName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (daySchedule.lessons.isEmpty()) {
            EmptyState()
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 80.dp) // Отступ снизу
            ) {
                items(
                    items = daySchedule.lessons,
                    key = { it.id }
                ) { lesson ->

                    Box(modifier = Modifier.animateItemPlacement()) {
                        LessonItem(
                            lesson = lesson,
                            onClick = { onLessonClick(lesson) },
                            onLongClick = { onLessonLongClick(lesson) }
                        )
                    }
                }
            }
        }
    }
}

// Компонент для пустой страницы
@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "В этот день занятий нет \uD83D\uDE34",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
            Text(
                text = "Отдыхайте!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewDaySchedule() {
    val lessons = listOf(
        Lesson(name = "Математика", room = "301", startTime = "08:30", endTime = "10:00"),
        Lesson(name = "Физкультура", room = "Спортзал", startTime = "10:10", endTime = "11:40"),
        Lesson(name = "Программирование", room = "Комп. класс", startTime = "12:00", endTime = "13:30", color = 0xFF4CAF50)
    )
    val day = DaySchedule(dayName = "Понедельник", lessons = lessons)

    DayScheduleView(
        daySchedule = day,
        onLessonLongClick = {},
        onLessonClick = {}
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewEmptyDay() {
    val day = DaySchedule(dayName = "Воскресенье", lessons = emptyList())
    DayScheduleView(
        daySchedule = day,
        onLessonLongClick = {},
        onLessonClick = {}
    )
}