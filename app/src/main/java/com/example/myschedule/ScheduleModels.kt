package com.example.myschedule

import kotlinx.serialization.Serializable
import java.util.UUID

// --- НОВОЕ: Типы расписания ---
enum class ScheduleType {
    Fixed,      // Одна неделя (всегда одинаковая)
    Rotation,   // Чередование (Неделя 1 / Неделя 2) - Твой текущий вариант
    Semester    // Семестр (Неделя 1, 2, 3... 18 - все разные)
}

// 1. Одно занятие (упрощенное)
@Serializable
data class Lesson(
    // Генерируем уникальный ID автоматически при создании, если не передан другой
    val id: String = UUID.randomUUID().toString(),
    val name: String,         // Название (например: "РМП (Лекция)")
    val room: String,         // Кабинет (например: "305" или "Онлайн")
    val startTime: String,    // Начало "08:00"
    val endTime: String,      // Конец "09:30"
    val color: Long? = null   // Цвет карточки (храним как число, так удобнее в Compose), null = стандартный
)

// 2. Описываем один день (Понедельник и т.д.)
@Serializable
data class DaySchedule(
    val dayName: String,      // Название дня (Понедельник)
    val lessons: List<Lesson> // Список пар в этот день
)

// 3. Описываем неделю (если у тебя чередуются недели, это пригодится)
@Serializable
data class WeekSchedule(
    val weekNumber: Int,      // Номер недели (1 или 2)
    val days: List<DaySchedule> // Список дней в этой неделе
)

// 4. Главный объект всего расписания
@Serializable
data class MainSchedule(
    val weeks: List<WeekSchedule>,
    val settings: ScheduleSettings = ScheduleSettings() // Задел на будущее (настройки)
)

// 5. Настройки (пока пустые, но пригодятся для темы оформления или дефолтной длительности пары)
@Serializable
data class ScheduleSettings(
    val defaultLessonDurationMinutes: Int = 90,
    val isWeekRotationEnabled: Boolean = true, //переход недель
    val isFirstLaunch: Boolean = true,

    val scheduleType: ScheduleType = ScheduleType.Rotation,
    val semesterStartDate: String? = null // Храним дату как строку "2024-09-01"
)