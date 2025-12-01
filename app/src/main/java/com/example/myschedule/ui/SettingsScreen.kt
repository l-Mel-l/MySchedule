package com.example.myschedule.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myschedule.ScheduleType
import com.example.myschedule.ScheduleViewModel
import com.example.myschedule.TimeUtils
import java.time.LocalDate
import java.util.Calendar

@Composable
fun SettingsScreen(
    viewModel: ScheduleViewModel,
    onShareClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.schedule?.settings ?: return
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showSemesterInfoDialog by remember { mutableStateOf(false) }

    // Функция открытия календаря (оставляем как была)
    fun showDatePicker() { /* ... код календаря ... */
        val calendar = Calendar.getInstance()
        val currentStart = TimeUtils.parseDate(settings.semesterStartDate)
        if (currentStart != null) {
            calendar.set(currentStart.year, currentStart.monthValue - 1, currentStart.dayOfMonth)
        }
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, day: Int ->
                val selectedDate = LocalDate.of(year, month + 1, day)
                viewModel.updateSemesterStartDate(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 24.dp))

        // Экспорт
        Card(
            onClick = onShareClick,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Поделиться расписанием", fontWeight = FontWeight.Bold)
            }
        }

        Divider(modifier = Modifier.padding(vertical = 24.dp))

        // Выбор режима
        Text("Режим расписания", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        ModeOption("Одна неделя", "Простое расписание", settings.scheduleType == ScheduleType.Fixed) {
            viewModel.updateScheduleType(ScheduleType.Fixed)
        }
        ModeOption("Две недели", "Четная / Нечетная", settings.scheduleType == ScheduleType.Rotation) {
            viewModel.updateScheduleType(ScheduleType.Rotation)
        }
        // 3. Семестр (Изменили onClick)
        ModeOption(
            title = "Семестр",
            description = "Каждая неделя уникальна (1, 2, 3...). Ручное переключение.",
            isSelected = settings.scheduleType == ScheduleType.Semester,
            onClick = {
                // Не переключаем сразу, а показываем инфо
                if (settings.scheduleType != ScheduleType.Semester) {
                    showSemesterInfoDialog = true
                }
            }
        )

        // --- НАСТРОЙКИ ДАТЫ (Показываем ТОЛЬКО для режима "Две недели" (Rotation)) ---
        if (settings.scheduleType == ScheduleType.Rotation) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Дата начала обучения",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Нужна, чтобы подсвечивать текущую неделю",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .clickable { showDatePicker() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CalendarToday, null, tint = Color.Gray)
                        Spacer(modifier = Modifier.width(16.dp))

                        val dateText = settings.semesterStartDate ?: "Нажмите, чтобы выбрать"
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (settings.semesterStartDate != null) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 24.dp))

        // Сброс
        Text("Управление данными", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Сбросить всё расписание", color = MaterialTheme.colorScheme.error)
        }
        // --- ДИАЛОГ ИНФОРМАЦИИ О СЕМЕСТРЕ ---
        if (showSemesterInfoDialog) {
            AlertDialog(
                onDismissRequest = { showSemesterInfoDialog = false },
                icon = { Icon(Icons.Default.Info, null) },
                title = { Text("Ручной режим") },
                text = {
                    Text("В режиме «Семестр» автоматическое переключение недель отключено.\n\n" +
                            "Из-за сложности учебных графиков, вам нужно будет самостоятельно выбирать текущую неделю из списка.")
                },
                confirmButton = {
                    Button(
                        onClick = {
                            // Только тут реально меняем настройку
                            viewModel.updateScheduleType(ScheduleType.Semester)
                            showSemesterInfoDialog = false
                        }
                    ) {
                        Text("Понятно, включить")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSemesterInfoDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Вы уверены?") },
            text = { Text("Все ваши занятия будут удалены безвозвратно.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllData(); showDeleteDialog = false }) { Text("Сбросить", color = Color.Red) }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") } }
        )
    }
}

// Компонент для выбора режима (Radio Button стиль)
@Composable
fun ModeOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null // Обработка клика на Row
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}