package com.example.myschedule.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
                    ScheduleType.Fixed -> { }

                    ScheduleType.Rotation -> {
                        val startDate = settings?.semesterStartDate
                        val realWeekNum = if (startDate != null) {
                            val absoluteWeek = TimeUtils.getCurrentWeekNumber(startDate)
                            if (absoluteWeek % 2 != 0) 1 else 2
                        } else 0

                        WeekSelector(
                            selectedWeek = uiState.selectedWeekNumber,
                            currentRealWeek = realWeekNum,
                            onWeekSelected = { viewModel.selectWeek(it) }
                        )
                    }

                    ScheduleType.Semester -> {
                        val existingWeeks = uiState.schedule?.weeks?.map { it.weekNumber } ?: emptyList()

                        SemesterSheetSelector(
                            selectedWeek = uiState.selectedWeekNumber,
                            currentRealWeek = 0,
                            existingWeeks = existingWeeks,
                            onWeekSelected = { viewModel.selectWeek(it) },
                            onAddWeek = {
                                val nextWeek = (existingWeeks.maxOrNull() ?: 0) + 1
                                viewModel.createNewWeek(nextWeek)
                            },
                            onDeleteWeek = { weekNum ->
                                viewModel.deleteWeek(weekNum)
                            }
                        )
                    }
                }

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
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (currentWeek == null) "Неделя ${uiState.selectedWeekNumber} не заполнена"
                                    else "В этот день занятий нет \uD83D\uDE34",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Нажми +, чтобы добавить занятия",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray.copy(alpha = 0.7f)
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