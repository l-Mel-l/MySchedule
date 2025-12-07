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
                val startWeek = when (settings.scheduleType) {
                    ScheduleType.Fixed -> 1
                    ScheduleType.Rotation -> {
                        // Если дата задана - считаем реальную (1 или 2). Иначе 1.
                        if (settings.semesterStartDate != null) {
                            val abs = TimeUtils.getCurrentWeekNumber(settings.semesterStartDate)
                            if (abs % 2 != 0) 1 else 2
                        } else 1
                    }
                    ScheduleType.Semester -> {
                        // Для семестра: если есть дата - показываем реальную, но только если она существует в файле
                        // Иначе показываем 1.
                        if (settings.semesterStartDate != null) {
                            val real = TimeUtils.getCurrentWeekNumber(settings.semesterStartDate)
                            // Проверяем, создал ли юзер такую неделю
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

            // --- ОБНОВЛЕНИЕ ВИДЖЕТА ---
            // Пытаемся обновить все экземпляры виджета на рабочем столе
            try {
                // Обновляем маленький виджет
                ScheduleWidget().updateAll(getApplication())
                // Обновляем большой виджет
                ScheduleListWidget().updateAll(getApplication())
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

        // --- УМНАЯ ЛОГИКА ---
        // Если пользователь включил "Две недели", а дата еще не задана...
        // ...давай зададим её по умолчанию (как будто сегодня началась 1-я неделя).
        // Тогда авто-переключение начнет работать сразу же.

        var newSettings = currentSchedule.settings.copy(scheduleType = newType)

        if (newType == ScheduleType.Rotation && newSettings.semesterStartDate == null) {
            val today = LocalDate.now()
            val daysToMinus = today.dayOfWeek.value - 1
            val thisMonday = today.minusDays(daysToMinus.toLong())
            val dateStr = TimeUtils.formatDate(thisMonday)

            newSettings = newSettings.copy(semesterStartDate = dateStr)
        }
        // ---------------------

        val newSchedule = currentSchedule.copy(settings = newSettings)
        saveSchedule(newSchedule)

        // Сбрасываем UI на 1-ю неделю
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

    // Удаление целой недели
    fun deleteWeek(weekNum: Int) {
        val currentSchedule = _uiState.value.schedule ?: return

        // Удаляем неделю из списка
        val updatedWeeks = currentSchedule.weeks.filter { it.weekNumber != weekNum }
        val newSchedule = currentSchedule.copy(weeks = updatedWeeks)

        saveSchedule(newSchedule)

        // Если мы удалили текущую выбранную неделю - переключаемся на какую-нибудь другую
        if (_uiState.value.selectedWeekNumber == weekNum) {
            val fallbackWeek = updatedWeeks.firstOrNull()?.weekNumber ?: 1
            selectWeek(fallbackWeek)
        }
    }

    // Умная настройка для "Двух недель"
    // Пользователь говорит: "Сейчас 1-я неделя". Мы сами вычисляем дату старта.
    fun setRotationCurrentWeek(isWeek1: Boolean) {
        val currentSchedule = _uiState.value.schedule ?: return

        // 1. Берем сегодняшний день
        val today = LocalDate.now()

        // 2. Находим понедельник текущей недели
        // (Java Time API: DayOfWeek.MONDAY = 1)
        val daysToMinus = today.dayOfWeek.value - 1
        val thisMonday = today.minusDays(daysToMinus.toLong())

        // 3. Вычисляем дату старта "семестра"
        // Если сейчас Неделя 1 -> то старт был в этот понедельник.
        // Если сейчас Неделя 2 -> то старт был неделю назад.
        val startDate = if (isWeek1) thisMonday else thisMonday.minusWeeks(1)

        // 4. Сохраняем
        updateSemesterStartDate(startDate)

        // 5. Сразу обновляем UI, чтобы переключатель перепрыгнул
        selectWeek(if (isWeek1) 1 else 2)
    }
}