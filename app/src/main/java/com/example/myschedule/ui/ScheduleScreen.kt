package com.example.myschedule.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myschedule.Lesson
import com.example.myschedule.ScheduleType
import com.example.myschedule.ScheduleViewModel
import com.example.myschedule.TimeUtils

@Composable
fun ScheduleScreen(viewModel: ScheduleViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var lessonToEdit by remember { mutableStateOf<Lesson?>(null) }
    var lessonToDelete by remember { mutableStateOf<Lesson?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            if (uiState.isLoading) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator()
                }
            } else {

                val settings = uiState.schedule?.settings
                val scheduleType = settings?.scheduleType ?: ScheduleType.Rotation

                when (scheduleType) {
                    ScheduleType.Fixed -> { /* Ничего */ }

                    ScheduleType.Rotation -> {
                        // --- ВОЗВРАЩАЕМ ПОДСВЕТКУ ---
                        val startDate = settings?.semesterStartDate
                        val realWeekNum = if (startDate != null) {
                            val absoluteWeek = TimeUtils.getCurrentWeekNumber(startDate)
                            // Если неделя нечетная (1, 3, 5...) -> Это "Неделя 1"
                            // Если неделя четная (2, 4, 6...) -> Это "Неделя 2"
                            if (absoluteWeek % 2 != 0) 1 else 2
                        } else 0

                        WeekSelector(
                            selectedWeek = uiState.selectedWeekNumber,
                            currentRealWeek = realWeekNum, // Передаем 1 или 2
                            onWeekSelected = { viewModel.selectWeek(it) }
                        )
                    }

                    ScheduleType.Semester -> {
                        val existingWeeks = uiState.schedule?.weeks?.map { it.weekNumber } ?: emptyList()
                        // Тут просто считаем абсолютную неделю для галочки в списке
                        val realWeek = TimeUtils.getCurrentWeekNumber(settings?.semesterStartDate)

                        SemesterSheetSelector(
                            selectedWeek = uiState.selectedWeekNumber,
                            existingWeeks = existingWeeks,
                            onWeekSelected = { viewModel.selectWeek(it) },
                            onAddWeek = {
                                val nextWeek = (existingWeeks.maxOrNull() ?: 0) + 1
                                viewModel.createNewWeek(nextWeek)
                            }
                        )
                    }
                }

                // ... (Дальше без изменений: DaySelector, Divider, Schedule List) ...
                // Скопируй остальную часть файла из предыдущего успешного варианта
                // (DaySelector, Divider, Логика отображения списка или EmptyState)

                DaySelector(
                    selectedDayIndex = uiState.selectedDayIndex,
                    onDaySelected = { viewModel.selectDay(it) }
                )

                Divider(color = Color.LightGray, thickness = 0.5.dp)

                val schedule = uiState.schedule
                if (schedule != null) {
                    val currentWeek = schedule.weeks.find { it.weekNumber == uiState.selectedWeekNumber }
                    val currentDay = currentWeek?.days?.getOrNull(uiState.selectedDayIndex)

                    if (currentDay != null && currentDay.lessons.isNotEmpty()) {
                        DayScheduleView(
                            daySchedule = currentDay,
                            onLessonLongClick = { lessonToDelete = it },
                            onLessonClick = {
                                lessonToEdit = it
                                showAddDialog = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Box(
                            modifier = Modifier.weight(1f).fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.EditCalendar,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = if (currentWeek == null) "Неделя ${uiState.selectedWeekNumber} еще не создана" else "В этот день занятий нет",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "Нажми +, чтобы добавить",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Добавить")
        }

        if (showAddDialog) {
            AddLessonDialog(
                lessonToEdit = lessonToEdit,
                onDismiss = { showAddDialog = false; lessonToEdit = null },
                onSave = { lesson ->
                    if (lessonToEdit == null) viewModel.addLesson(uiState.selectedDayIndex, lesson)
                    else viewModel.editLesson(uiState.selectedDayIndex, lesson)
                    showAddDialog = false
                    lessonToEdit = null
                }
            )
        }

        if (lessonToDelete != null) {
            AlertDialog(
                onDismissRequest = { lessonToDelete = null },
                title = { Text("Удалить занятие?") },
                text = { Text("Вы действительно хотите удалить \"${lessonToDelete?.name}\"?") },
                confirmButton = {
                    TextButton(onClick = {
                        lessonToDelete?.let { viewModel.deleteLesson(uiState.selectedDayIndex, it.id) }
                        lessonToDelete = null
                    }) { Text("Удалить", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { lessonToDelete = null }) { Text("Отмена") }
                }
            )
        }
    }
}