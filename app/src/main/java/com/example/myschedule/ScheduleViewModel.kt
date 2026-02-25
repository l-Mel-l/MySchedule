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
    private fun loadSchedule() {
        viewModelScope.launch {
            val loadedSchedule = repository.loadSchedule()

            if (loadedSchedule != null) {
                var settings = loadedSchedule.settings
                if (settings.scheduleType == ScheduleType.Rotation && !settings.isWeekRotationEnabled) {
                    settings = settings.copy(scheduleType = ScheduleType.Fixed)
                }
                val startWeek = when (settings.scheduleType) {
                    ScheduleType.Fixed -> 1
                    ScheduleType.Rotation -> {
                        if (settings.semesterStartDate != null) {
                            val abs = TimeUtils.getCurrentWeekNumber(settings.semesterStartDate)
                            if (abs % 2 != 0) 1 else 2
                        } else 1
                    }
                    ScheduleType.Semester -> {
                        if (settings.semesterStartDate != null) {
                            val real = TimeUtils.getCurrentWeekNumber(settings.semesterStartDate)
                            val max = loadedSchedule.weeks.maxOfOrNull { it.weekNumber } ?: 1
                            if (real <= max) real else 1
                        } else 1
                    }
                }

                _uiState.update {
                    it.copy(
                        schedule = loadedSchedule.copy(settings = settings),
                        isLoading = false,
                        selectedWeekNumber = startWeek
                    )
                }
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

    private fun createEmptyDays(): List<DaySchedule> {
        val dayNames = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
        return dayNames.map { DaySchedule(it, emptyList()) }
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

        var newSettings = currentSchedule.settings.copy(scheduleType = newType)

        if (newType == ScheduleType.Rotation && newSettings.semesterStartDate == null) {
            val today = LocalDate.now()
            val daysToMinus = today.dayOfWeek.value - 1
            val thisMonday = today.minusDays(daysToMinus.toLong())
            val dateStr = TimeUtils.formatDate(thisMonday)

            newSettings = newSettings.copy(semesterStartDate = dateStr)
        }

        val newSchedule = currentSchedule.copy(settings = newSettings)
        saveSchedule(newSchedule)

        selectWeek(1)
    }
    fun updateSemesterStartDate(date: LocalDate) {
        val currentSchedule = _uiState.value.schedule ?: return
        val dateStr = TimeUtils.formatDate(date)
        val newSettings = currentSchedule.settings.copy(semesterStartDate = dateStr)
        saveSchedule(currentSchedule.copy(settings = newSettings))
    }

    fun addLesson(dayIndex: Int, lesson: Lesson) {
        val currentSchedule = _uiState.value.schedule ?: return
        val targetWeekNum = _uiState.value.selectedWeekNumber

        val weekExists = currentSchedule.weeks.any { it.weekNumber == targetWeekNum }

        val updatedWeeks = if (weekExists) {
            currentSchedule.weeks.map { week ->
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
            (currentSchedule.weeks + newWeek).sortedBy { it.weekNumber }
        }

        saveSchedule(currentSchedule.copy(weeks = updatedWeeks))
    }

    fun createNewWeek(weekNum: Int) {
        val currentSchedule = _uiState.value.schedule ?: return
        if (currentSchedule.weeks.any { it.weekNumber == weekNum }) {
            selectWeek(weekNum)
            return
        }
        val newDays = createEmptyDays()
        val newWeek = WeekSchedule(weekNum, newDays)
        val updatedWeeks = (currentSchedule.weeks + newWeek).sortedBy { it.weekNumber }

        saveSchedule(currentSchedule.copy(weeks = updatedWeeks))
        selectWeek(weekNum)
    }

    fun editLesson(dayIndex: Int, updatedLesson: Lesson) {
        val currentSchedule = _uiState.value.schedule ?: return
        val updatedWeeks = currentSchedule.weeks.map { week ->
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
        saveSchedule(currentSchedule.copy(weeks = updatedWeeks))
    }

    fun deleteLesson(dayIndex: Int, lessonId: String) {
        val currentSchedule = _uiState.value.schedule ?: return
        val updatedWeeks = currentSchedule.weeks.map { week ->
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
        saveSchedule(currentSchedule.copy(weeks = updatedWeeks))
    }

    fun handleImport(uri: android.net.Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            val importedSchedule = repository.readScheduleFromUri(uri)
            if (importedSchedule != null) {
                val currentSettings = _uiState.value.schedule?.settings ?: ScheduleSettings()
                val mergedSettings = importedSchedule.settings.copy(
                    isFirstLaunch = currentSettings.isFirstLaunch,
                )
                saveSchedule(importedSchedule.copy(settings = mergedSettings))
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

        val updatedWeeks = currentSchedule.weeks.filter { it.weekNumber != weekNum }
        val newSchedule = currentSchedule.copy(weeks = updatedWeeks)

        saveSchedule(newSchedule)

        if (_uiState.value.selectedWeekNumber == weekNum) {
            val fallbackWeek = updatedWeeks.firstOrNull()?.weekNumber ?: 1
            selectWeek(fallbackWeek)
        }
    }

    fun setRotationCurrentWeek(isWeek1: Boolean) {
        val currentSchedule = _uiState.value.schedule ?: return

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