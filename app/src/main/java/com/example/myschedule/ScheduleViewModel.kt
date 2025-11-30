package com.example.myschedule

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

// Состояние экрана
data class ScheduleUiState(
    val schedule: MainSchedule? = null,
    val isLoading: Boolean = true,
    val selectedDayIndex: Int = LocalDate.now().dayOfWeek.value - 1,
    val selectedWeekNumber: Int = 1,
    // isWeekRotationEnabled удаляем из стейта, теперь смотрим в settings.scheduleType
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ScheduleRepository(application)
    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            val loadedSchedule = repository.loadSchedule()

            if (loadedSchedule != null) {
                // МИГРАЦИЯ: Если старый файл
                var settings = loadedSchedule.settings
                if (settings.scheduleType == ScheduleType.Rotation && !settings.isWeekRotationEnabled) {
                    settings = settings.copy(scheduleType = ScheduleType.Fixed)
                }

                _uiState.update {
                    it.copy(
                        schedule = loadedSchedule.copy(settings = settings),
                        isLoading = false,
                        // ВСЕГДА начинаем с 1-й недели при старте, чтобы не было багов с "Неделя 13"
                        selectedWeekNumber = 1
                    )
                }
            } else {
                createEmptySchedule()
            }
        }
    }

    private fun createEmptySchedule() {
        // Создаем базовые 1 и 2 недели
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
        val newSettings = currentSchedule.settings.copy(scheduleType = newType)
        val newSchedule = currentSchedule.copy(settings = newSettings)

        saveSchedule(newSchedule)

        // При смене типа ВСЕГДА сбрасываем на 1 неделю
        selectWeek(1)
    }

    // Сохранение даты старта (пока просто сохраняем, не переключаем недели)
    fun updateSemesterStartDate(date: LocalDate) {
        val currentSchedule = _uiState.value.schedule ?: return
        val dateStr = TimeUtils.formatDate(date)
        val newSettings = currentSchedule.settings.copy(semesterStartDate = dateStr)
        saveSchedule(currentSchedule.copy(settings = newSettings))
    }

    // --- УПРАВЛЕНИЕ УРОКАМИ ---

    fun addLesson(dayIndex: Int, lesson: Lesson) {
        val currentSchedule = _uiState.value.schedule ?: return
        val targetWeekNum = _uiState.value.selectedWeekNumber

        // Проверяем, существует ли такая неделя
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
            // Создаем новую неделю, если её нет
            val newDays = createEmptyDays().toMutableList()
            val targetDay = newDays[dayIndex]
            newDays[dayIndex] = targetDay.copy(lessons = listOf(lesson))

            val newWeek = WeekSchedule(targetWeekNum, newDays)
            (currentSchedule.weeks + newWeek).sortedBy { it.weekNumber }
        }

        saveSchedule(currentSchedule.copy(weeks = updatedWeeks))
    }

    // Создание новой недели (пустой)
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

    // Импорт и другие функции оставляем как были...
    // (copy-paste import logic from previous steps if needed)
    fun handleImport(uri: android.net.Uri, onSuccess: () -> Unit, onError: () -> Unit) {
        // ... (код импорта, который мы писали ранее)
        // Если нужно, я могу его продублировать, но думаю он у тебя сохранился
        viewModelScope.launch {
            val importedSchedule = repository.readScheduleFromUri(uri)
            if (importedSchedule != null) {
                val currentSettings = _uiState.value.schedule?.settings ?: ScheduleSettings()
                val mergedSettings = importedSchedule.settings.copy(
                    isFirstLaunch = currentSettings.isFirstLaunch,
                    // Оставляем свои настройки типа, если хотим, или берем чужие.
                    // При импорте логично взять чужую структуру недель.
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
}