package com.example.myschedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.glance.appwidget.updateAll
import com.example.myschedule.glance.ScheduleWidget
import com.example.myschedule.glance.ScheduleListWidget
import com.example.myschedule.notification.TimerController
import com.example.myschedule.wear.WearDataSender


data class ScheduleUiState(
    val schedule: MainSchedule? = null,
    val isLoading: Boolean = true,
    val selectedDayIndex: Int = LocalDate.now().dayOfWeek.value - 1,
    val selectedWeekNumber: Int = 1,
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)
    private val wearSender = WearDataSender(application)
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    private fun currentWeekToShow(schedule: MainSchedule): Int {
        val existing = schedule.activeWeeks.map { it.weekNumber }
        val real = schedule.weekNumberFor(LocalDate.now())
        return if (real in existing) real else existing.minOrNull() ?: 1
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            val loadedSchedule = repository.loadSchedule()

            if (loadedSchedule != null) {
                _uiState.update {
                    it.copy(
                        schedule = loadedSchedule,
                        isLoading = false,
                        selectedWeekNumber = currentWeekToShow(loadedSchedule)
                    )
                }

                TimerController.refresh(getApplication())
            } else {
                createEmptySchedule()
            }
        }
    }

    private fun createEmptySchedule() {
        val weeks = listOf(
            WeekSchedule(1, createEmptyDays()),
            WeekSchedule(2, createEmptyDays())
        )
        val newSchedule = MainSchedule(weeks)
        saveSchedule(newSchedule)
    }

    fun saveSchedule(newSchedule: MainSchedule) {
        viewModelScope.launch {
            repository.saveSchedule(newSchedule)
            _uiState.update { it.copy(schedule = newSchedule, isLoading = false) }

            try {
                ScheduleWidget().updateAll(getApplication())
                ScheduleListWidget().updateAll(getApplication())
            } catch (e: Exception) {
                e.printStackTrace()
            }
            wearSender.sendScheduleToWatch(newSchedule)

            TimerController.refresh(getApplication())
        }
    }

    // Переключение дня
    fun selectDay(index: Int) {
        _uiState.update { it.copy(selectedDayIndex = index) }
    }

    // Переключение недели
    fun selectWeek(weekNumber: Int) {
        _uiState.update { it.copy(selectedWeekNumber = weekNumber) }
    }

    // Смена режима расписания
    fun updateScheduleType(newType: ScheduleType) {
        val currentSchedule = _uiState.value.schedule ?: return

        var newSchedule = currentSchedule.copy(
            settings = currentSchedule.settings.copy(scheduleType = newType)
        )

        if (newType == ScheduleType.Rotation && newSchedule.settings.semesterStartDate == null) {
            val today = LocalDate.now()
            val daysToMinus = today.dayOfWeek.value - 1
            val thisMonday = today.minusDays(daysToMinus.toLong())
            val dateStr = TimeUtils.formatDate(thisMonday)

            newSchedule = newSchedule.copy(
                settings = newSchedule.settings.copy(semesterStartDate = dateStr)
            )
        }

        if (newType == ScheduleType.Semester && newSchedule.semesterWeeks.isEmpty()) {
            newSchedule = newSchedule.copy(
                semesterWeeks = listOf(WeekSchedule(1, createEmptyDays()))
            )
        }

        saveSchedule(newSchedule)
        selectWeek(currentWeekToShow(newSchedule))
    }

    fun updateSemesterStartDate(date: LocalDate) {
        val currentSchedule = _uiState.value.schedule ?: return
        val dateStr = TimeUtils.formatDate(date)
        val newSettings = currentSchedule.settings.copy(semesterStartDate = dateStr)
        saveSchedule(currentSchedule.copy(settings = newSettings))
    }

    fun updateSemesterModeStartDate(date: LocalDate) {
        val currentSchedule = _uiState.value.schedule ?: return
        val newSchedule = currentSchedule.copy(
            settings = currentSchedule.settings.copy(
                semesterModeStartDate = TimeUtils.formatDate(date)
            )
        )
        saveSchedule(newSchedule)
        selectWeek(currentWeekToShow(newSchedule))
    }

    fun addLesson(dayIndex: Int, lesson: Lesson) {
        val currentSchedule = _uiState.value.schedule ?: return
        val targetWeekNum = _uiState.value.selectedWeekNumber
        val weeks = currentSchedule.activeWeeks

        val weekExists = weeks.any { it.weekNumber == targetWeekNum }

        val updatedWeeks = if (weekExists) {
            weeks.map { week ->
                if (week.weekNumber == targetWeekNum) {
                    val updatedDays = week.days.mapIndexed { index, day ->
                        if (index == dayIndex) {
                            val newLessons = (day.lessons + lesson).sortedBy { it.startTime }
                            day.copy(lessons = newLessons)
                        } else day
                    }
                    week.copy(days = updatedDays)
                } else week
            }
        } else {
            val newDays = createEmptyDays().toMutableList()
            val targetDay = newDays[dayIndex]
            newDays[dayIndex] = targetDay.copy(lessons = listOf(lesson))

            val newWeek = WeekSchedule(targetWeekNum, newDays)
            (weeks + newWeek).sortedBy { it.weekNumber }
        }

        saveSchedule(currentSchedule.withActiveWeeks(updatedWeeks))
    }

    fun createNewWeek(weekNum: Int) {
        val currentSchedule = _uiState.value.schedule ?: return
        val weeks = currentSchedule.activeWeeks
        if (weeks.any { it.weekNumber == weekNum }) {
            selectWeek(weekNum)
            return
        }
        val newWeek = WeekSchedule(weekNum, createEmptyDays())
        val updatedWeeks = (weeks + newWeek).sortedBy { it.weekNumber }

        saveSchedule(currentSchedule.withActiveWeeks(updatedWeeks))
        selectWeek(weekNum)
    }

    fun editLesson(dayIndex: Int, updatedLesson: Lesson) {
        val currentSchedule = _uiState.value.schedule ?: return
        val updatedWeeks = currentSchedule.activeWeeks.map { week ->
            if (week.weekNumber == _uiState.value.selectedWeekNumber) {
                val updatedDays = week.days.mapIndexed { index, day ->
                    if (index == dayIndex) {
                        val newLessons = day.lessons.map {
                            if (it.id == updatedLesson.id) updatedLesson else it
                        }.sortedBy { it.startTime }
                        day.copy(lessons = newLessons)
                    } else day
                }
                week.copy(days = updatedDays)
            } else week
        }
        saveSchedule(currentSchedule.withActiveWeeks(updatedWeeks))
    }

    fun deleteLesson(dayIndex: Int, lessonId: String) {
        val currentSchedule = _uiState.value.schedule ?: return
        val updatedWeeks = currentSchedule.activeWeeks.map { week ->
            if (week.weekNumber == _uiState.value.selectedWeekNumber) {
                val updatedDays = week.days.mapIndexed { index, day ->
                    if (index == dayIndex) {
                        val newLessons = day.lessons.filter { it.id != lessonId }
                        day.copy(lessons = newLessons)
                    } else day
                }
                week.copy(days = updatedDays)
            } else week
        }
        saveSchedule(currentSchedule.withActiveWeeks(updatedWeeks))
    }

    fun handleImport(uri: android.net.Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val importedSchedule = repository.readScheduleFromUri(uri)
            if (importedSchedule != null) {
                val currentSettings = _uiState.value.schedule?.settings ?: ScheduleSettings()
                val mergedSettings = importedSchedule.settings.copy(
                    isFirstLaunch = currentSettings.isFirstLaunch,
                )
                val merged = importedSchedule.copy(settings = mergedSettings)
                saveSchedule(merged)
                selectWeek(currentWeekToShow(merged))
                onSuccess()
            } else {
                onError()
            }
        }
    }

    fun disableFirstLaunch() {
        val currentSchedule = _uiState.value.schedule ?: return
        val newSettings = currentSchedule.settings.copy(isFirstLaunch = false)
        saveSchedule(currentSchedule.copy(settings = newSettings))
    }
    fun clearAllData() {
        createEmptySchedule()
    }

    fun deleteWeek(weekNum: Int) {
        val currentSchedule = _uiState.value.schedule ?: return
        val weeks = currentSchedule.activeWeeks

        if (weeks.size <= 1) return

        val updatedWeeks = weeks.filter { it.weekNumber != weekNum }
        saveSchedule(currentSchedule.withActiveWeeks(updatedWeeks))

        if (_uiState.value.selectedWeekNumber == weekNum) {
            val fallbackWeek = updatedWeeks.firstOrNull()?.weekNumber ?: 1
            selectWeek(fallbackWeek)
        }
    }

    fun renameWeek(weekNum: Int, newName: String) {
        val currentSchedule = _uiState.value.schedule ?: return
        val updatedWeeks = currentSchedule.activeWeeks.map {
            if (it.weekNumber == weekNum) it.copy(name = newName.trim()) else it
        }
        saveSchedule(currentSchedule.withActiveWeeks(updatedWeeks))
    }

    fun setRotationCurrentWeek(isWeek1: Boolean) {
        if (_uiState.value.schedule == null) return

        val today = LocalDate.now()
        val daysToMinus = today.dayOfWeek.value - 1
        val thisMonday = today.minusDays(daysToMinus.toLong())

        val startDate = if (isWeek1) thisMonday else thisMonday.minusWeeks(1)
        updateSemesterStartDate(startDate)

        selectWeek(if (isWeek1) 1 else 2)
    }

    fun syncToWatch() {
        val currentSchedule = _uiState.value.schedule ?: return

        viewModelScope.launch {
            wearSender.sendScheduleToWatch(currentSchedule)
        }
    }
}
