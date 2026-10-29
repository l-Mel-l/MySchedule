package com.example.myschedule

import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.util.UUID

enum class ScheduleType {
    Fixed,
    Rotation,
    Semester
}


@Serializable
data class Lesson(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val room: String,
    val startTime: String,
    val endTime: String,
    val color: Long? = null,
    val note: String = ""
)


@Serializable
data class DaySchedule(
    val dayName: String,
    val lessons: List<Lesson>
)


@Serializable
data class WeekSchedule(
    val weekNumber: Int,
    val days: List<DaySchedule>,
    val name: String = ""
)


@Serializable
data class MainSchedule(
    val weeks: List<WeekSchedule>,
    val settings: ScheduleSettings = ScheduleSettings(),
    val semesterWeeks: List<WeekSchedule> = emptyList()
)


@Serializable
data class ScheduleSettings(
    val defaultLessonDurationMinutes: Int = 90,
    val isWeekRotationEnabled: Boolean = true,
    val isFirstLaunch: Boolean = true,

    val scheduleType: ScheduleType = ScheduleType.Fixed,
    val semesterStartDate: String? = null,
    val semesterModeStartDate: String? = null
)

fun MainSchedule.weekNumberFor(date: LocalDate): Int {
    val s = settings
    val type = if (s.scheduleType == ScheduleType.Rotation && !s.isWeekRotationEnabled) {
        ScheduleType.Fixed
    } else {
        s.scheduleType
    }

    return when (type) {
        ScheduleType.Fixed -> 1
        ScheduleType.Rotation -> {
            val start = s.semesterStartDate ?: return 1
            val absolute = TimeUtils.getWeekNumberForDate(start, date)
            if (absolute % 2 != 0) 1 else 2
        }
        ScheduleType.Semester -> {
            val start = s.semesterModeStartDate
                ?: return semesterWeeks.minOfOrNull { it.weekNumber } ?: 1
            TimeUtils.getSemesterWeekNumber(start, date)
        }
    }
}

val MainSchedule.activeWeeks: List<WeekSchedule>
    get() = if (settings.scheduleType == ScheduleType.Semester) semesterWeeks else weeks

fun MainSchedule.withActiveWeeks(newWeeks: List<WeekSchedule>): MainSchedule =
    if (settings.scheduleType == ScheduleType.Semester) copy(semesterWeeks = newWeeks)
    else copy(weeks = newWeeks)

fun WeekSchedule.displayName(): String = name.ifBlank { "Неделя $weekNumber" }

fun createEmptyDays(): List<DaySchedule> {
    val dayNames = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
    return dayNames.map { DaySchedule(it, emptyList()) }
}

fun MainSchedule.healed(): MainSchedule {
    var result = this
    var st = result.settings

    if (st.scheduleType == ScheduleType.Rotation && !st.isWeekRotationEnabled) {
        st = st.copy(scheduleType = ScheduleType.Fixed)
    }
    if (st.scheduleType == ScheduleType.Semester && result.semesterWeeks.isEmpty() && result.weeks.isNotEmpty()) {
        result = result.copy(semesterWeeks = result.weeks)
        if (st.semesterModeStartDate == null) st = st.copy(semesterModeStartDate = st.semesterStartDate)
    }
    result = result.copy(settings = st)

    if (result.weeks.isEmpty()) {
        result = result.copy(weeks = listOf(WeekSchedule(1, createEmptyDays())))
    }
    if (st.scheduleType == ScheduleType.Semester && result.semesterWeeks.isEmpty()) {
        result = result.copy(semesterWeeks = listOf(WeekSchedule(1, createEmptyDays())))
    }
    return result
}