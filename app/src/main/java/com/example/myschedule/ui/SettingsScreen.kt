package com.example.myschedule.ui

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileOpen
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight

@Composable
fun SettingsScreen(
    viewModel: ScheduleViewModel,
    onShareClick: () -> Unit,
    onSyncWatchClick: () -> Unit,
    onOpenNotificationSettings: () -> Unit = {},
    onOpenAppearance: () -> Unit = {},
    triggerImport: Boolean = false,
    onImportTriggered: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.schedule?.settings ?: return
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            pendingImportUri = uri
            showImportConfirmDialog = true
        }
    }

    LaunchedEffect(triggerImport) {
        if (triggerImport) {
            importLauncher.launch(arrayOf("application/json"))
            onImportTriggered()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(16.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Card(
                onClick = onShareClick,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Поделиться расписанием", fontWeight = FontWeight.Bold)
                }
            }
            Card(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Импортировать расписание", fontWeight = FontWeight.Bold)
                }
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

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        val notificationsOn = remember { com.example.myschedule.notification.NotificationPrefs(context).enabled }
        Card(
            onClick = onOpenNotificationSettings,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Уведомление с таймером", fontWeight = FontWeight.Bold)
                    Text(
                        if (notificationsOn) "Включено" else "Выключено",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            onClick = onOpenAppearance,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Palette, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Внешний вид", fontWeight = FontWeight.Bold)
                    Text(
                        "Тема, цвета, карточки занятий, круг таймера",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

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
            description = "У каждой недели свои занятия (1, 2, 3...). Недели переключаются сами по дате начала семестра.",
            isSelected = settings.scheduleType == ScheduleType.Semester,
            onClick = { viewModel.updateScheduleType(ScheduleType.Semester) }
        )

        if (settings.scheduleType == ScheduleType.Semester) {
            Spacer(modifier = Modifier.height(16.dp))
            val semStart = TimeUtils.parseDate(settings.semesterModeStartDate)
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Начало семестра",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Первая неделя семестра. От неё приложение считает, какая неделя идёт сейчас. Недели семестра хранятся отдельно от других режимов.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            showSemesterDatePicker(context, semStart) { viewModel.updateSemesterModeStartDate(it) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            semStart?.format(
                                java.time.format.DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale.forLanguageTag("ru"))
                            ) ?: "Выбрать дату"
                        )
                    }
                }
            }
        }

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

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

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

    if (showImportConfirmDialog && pendingImportUri != null) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false; pendingImportUri = null },
            title = { Text("Импорт расписания") },
            text = {
                Column {
                    Text("Вы выбрали файл расписания.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Хотите заменить текущее расписание на новое? Старые данные будут удалены.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingImportUri?.let { uri ->
                            viewModel.handleImport(
                                uri = uri,
                                onSuccess = {
                                    android.widget.Toast.makeText(context, "Расписание импортировано!", android.widget.Toast.LENGTH_SHORT).show()
                                },
                                onError = {
                                    android.widget.Toast.makeText(context, "Ошибка импорта файла", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        showImportConfirmDialog = false
                        pendingImportUri = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text("Заменить") }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false; pendingImportUri = null }) {
                    Text("Отмена")
                }
            }
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