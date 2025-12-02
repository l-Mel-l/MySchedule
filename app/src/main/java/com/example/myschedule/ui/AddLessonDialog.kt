package com.example.myschedule.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.myschedule.Lesson

@Composable
fun AddLessonDialog(
    lessonToEdit: Lesson? = null,
    onDismiss: () -> Unit,
    onSave: (Lesson) -> Unit
) {
    var name by remember { mutableStateOf(lessonToEdit?.name ?: "") }
    var room by remember { mutableStateOf(lessonToEdit?.room ?: "") }
    var startTime by remember { mutableStateOf(lessonToEdit?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(lessonToEdit?.endTime ?: "09:30") }
    var selectedColor by remember { mutableStateOf(lessonToEdit?.color) }

    // --- НОВАЯ ПЕРЕМЕННАЯ ---
    var note by remember { mutableStateOf(lessonToEdit?.note ?: "") }

    var isNoteFieldVisible by remember { mutableStateOf(note.isNotEmpty()) }

    var errorMessage by remember { mutableStateOf("") }

    val colorPalette = listOf(null, 0xFFEF5350, 0xFF42A5F5, 0xFF66BB6A, 0xFFAB47BC)

    fun validateAndSave() {
        if (name.isBlank()) {
            errorMessage = "Введите название предмета"
            return
        }
        if (startTime >= endTime) {
            errorMessage = "Время конца должно быть позже начала"
            return
        }

        val resultLesson = lessonToEdit?.copy(
            name = name, room = room, startTime = startTime, endTime = endTime, color = selectedColor,
            note = note // --- СОХРАНЯЕМ ЗАМЕТКУ ---
        ) ?: Lesson(
            name = name, room = room, startTime = startTime, endTime = endTime, color = selectedColor,
            note = note // --- СОХРАНЯЕМ ЗАМЕТКУ ---
        )
        onSave(resultLesson)
    }

    val context = LocalContext.current
    fun showTimePicker(current: String, onTimeSelected: (String) -> Unit) {
        val parts = current.split(":")
        val hour = parts.getOrElse(0) { "08" }.toIntOrNull() ?: 8
        val minute = parts.getOrElse(1) { "00" }.toIntOrNull() ?: 0
        TimePickerDialog(context, { _, h, m -> onTimeSelected(String.format("%02d:%02d", h, m)) }, hour, minute, true).show()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(text = if (lessonToEdit == null) "Новое занятие" else "Редактирование", style = MaterialTheme.typography.headlineSmall)

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = "" },
                    label = { Text("Предмет") },
                    isError = errorMessage.isNotEmpty(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Кабинет") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TimeButton("Начало", startTime, { showTimePicker(startTime) { startTime = it } }, Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    TimeButton("Конец", endTime, { showTimePicker(endTime) { endTime = it } }, Modifier.weight(1f))
                }

                // Выбор цвета
                Text("Цвет метки", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    colorPalette.forEach { colorHex ->
                        val color = if (colorHex != null) Color(colorHex) else MaterialTheme.colorScheme.primary
                        val isSelected = selectedColor == colorHex
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(color)
                            .border(if (isSelected) 3.dp else 0.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                            .clickable { selectedColor = colorHex })
                    }
                }

                // --- ЛОГИКА СКРЫТИЯ ЗАМЕТКИ ---
                if (isNoteFieldVisible) {
                    // Если поле открыто - показываем TextField
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Заметка / ДЗ") },
                        placeholder = { Text("Например: Стр. 45, упр. 2") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        // Добавим крестик, чтобы можно было скрыть поле и очистить заметку
                        trailingIcon = {
                            IconButton(onClick = {
                                note = ""
                                isNoteFieldVisible = false
                            }) {
                                Icon(androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Удалить заметку")
                            }
                        }
                    )
                } else {
                    // Если поле скрыто - показываем кнопку
                    TextButton(
                        onClick = { isNoteFieldVisible = true },
                        modifier = Modifier.align(Alignment.Start) // Прижмем влево
                    ) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Edit, // Иконка карандаша
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Добавить заметку")
                    }
                }


                if (errorMessage.isNotEmpty()) Text(errorMessage, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Отмена") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { validateAndSave() }) { Text("Сохранить") }
                }
            }
        }
    }
}

@Composable
fun TimeButton(
    label: String,
    time: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(modifier = modifier.clickable { onClick() }) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, null, Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = time, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}