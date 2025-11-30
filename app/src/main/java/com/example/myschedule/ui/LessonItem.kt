package com.example.myschedule.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myschedule.Lesson
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LessonItem(
    lesson: Lesson,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Основной контейнер - Карточка с тенью
    Card(
        modifier = modifier
            .fillMaxWidth() // Занимает всю ширину
            .padding(vertical = 4.dp, horizontal = 8.dp) // Отступы снаружи
            .combinedClickable(
                onClick = { onClick() }, // Обычное нажатие (пока пустое, можно потом сделать редактирование)
                onLongClick = { onLongClick() } // Долгое нажатие -> Удаление
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Тень
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface // Цвет фона (белый/темный)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp), // Отступы внутри карточки
            verticalAlignment = Alignment.CenterVertically // Выравнивание по центру по вертикали
        ) {
            // 1. Колонка со временем (Слева)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp) // Фиксированная ширина для времени
            ) {
                Text(
                    text = lesson.startTime,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = lesson.endTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Вертикальная цветная полоска (Акцент)
            // Если цвет не задан, берем Оранжевый по умолчанию
            val accentColor = if (lesson.color != null) Color(lesson.color) else Color(0xFFFFA500)

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(50)) // Закругленные края полоски
                    .background(accentColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // 3. Информация о паре (Справа)
            Column(
                modifier = Modifier.weight(1f) // Занимает всё оставшееся место
            ) {
                Text(
                    text = lesson.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black, // Очень жирный шрифт для названия
                    maxLines = 2 // Максимум 2 строки
                )
                if (lesson.room.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lesson.room,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

// --- ПРЕДПРОСМОТР (PREVIEW) ---
// Эта штука позволяет видеть результат прямо в Android Studio без запуска на телефоне
@Preview(showBackground = true)
@Composable
fun PreviewLessonItem() {
    // Создаем фейковое занятие для теста
    val testLesson = Lesson(
        name = "Мобильная разработка",
        room = "Кабинет 404",
        startTime = "10:10",
        endTime = "11:40"
    )

    // Рисуем его
    LessonItem(
        lesson = testLesson,
        onLongClick = {},
        onClick = {}// <--- Просто пустая скобка, так как в превью кликать не будем
    )
}