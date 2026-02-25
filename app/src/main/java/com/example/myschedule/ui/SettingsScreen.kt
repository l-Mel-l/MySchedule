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
import androidx.compose.material.icons.filled.Watch

@Composable
fun SettingsScreen(
    viewModel: ScheduleViewModel,
    onShareClick: () -> Unit,
    onSyncWatchClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.schedule?.settings ?: return
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showSemesterInfoDialog by remember { mutableStateOf(false) }

    fun showDatePicker() {
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
            Card(
                onClick = {
                    onSyncWatchClick()
                    android.widget.Toast.makeText(context, "Данные отправлены на часы", android.widget.Toast.LENGTH_SHORT).show()
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Watch, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Отправить на часы", fontWeight = FontWeight.Bold)
                }
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
        // Семестр
        ModeOption(
            title = "Семестр",
            description = "Каждая неделя уникальна (1, 2, 3...). Ручное переключение.",
            isSelected = settings.scheduleType == ScheduleType.Semester,
            onClick = {
                if (settings.scheduleType != ScheduleType.Semester) {
                    showSemesterInfoDialog = true
                }
            }
        )

        if (settings.scheduleType == ScheduleType.Rotation) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Какая сейчас неделя?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Нужна, чтобы автоматически переключать Четную/Нечетную неделю.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val currentWeekReal = if (settings.semesterStartDate != null) {
                        val abs = TimeUtils.getCurrentWeekNumber(settings.semesterStartDate)
                        if (abs % 2 != 0) 1 else 2
                    } else 0

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Кнопка "Неделя 1"
                        Button(
                            onClick = { viewModel.setRotationCurrentWeek(isWeek1 = true) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentWeekReal == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (currentWeekReal == 1) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Сейчас 1-я")
                        }

                        // Кнопка "Неделя 2"
                        Button(
                            onClick = { viewModel.setRotationCurrentWeek(isWeek1 = false) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (currentWeekReal == 2) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (currentWeekReal == 2) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text("Сейчас 2-я")
                        }
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
            onClick = null
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}