package com.example.myschedule.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myschedule.Lesson

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LessonItem(
    lesson: Lesson,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Если цвет не задан, берем стандартный (например, оранжевый или из темы)
    val accentColor = if (lesson.color != null) Color(lesson.color) else Color(0xFFFFA500)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        // ГЛАВНЫЙ КОНТЕЙНЕР: Вертикальный (Сверху инфо, снизу заметка)
        Column {

            // --- ВЕРХНЯЯ ЧАСТЬ (Время, Полоска, Название) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Отступы для верхней части
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Время
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(60.dp)
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

                // 2. Цветная полоска
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(40.dp)
                        .clip(RoundedCornerShape(50))
                        .background(accentColor)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // 3. Название и кабинет
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lesson.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        maxLines = 2
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

            // --- НИЖНЯЯ ЧАСТЬ (Заметка) ---
            // Показываем только если заметка не пустая
            if (lesson.note.isNotEmpty()) {
                // Тонкий разделитель
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Сама заметка с иконкой
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, // Оранжевая иконка
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = lesson.note,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 10 // Ограничение строк, чтобы карточка не стала бесконечной
                    )
                }
            }
        }
    }
}

// --- ПРЕДПРОСМОТР ---
@Preview(showBackground = true)
@Composable
fun PreviewLessonItem() {
    val testLesson = Lesson(
        name = "Программирование",
        room = "404",
        startTime = "12:00",
        endTime = "13:30",
        note = "Сдать лабораторную работу №2 до конца недели!"
    )

    LessonItem(
        lesson = testLesson,
        onClick = {},
        onLongClick = {}
    )
}