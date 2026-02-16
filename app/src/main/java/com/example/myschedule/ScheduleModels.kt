package com.example.myschedule

import kotlinx.serialization.Serializable
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
    val days: List<DaySchedule>
)


@Serializable
data class MainSchedule(
    val weeks: List<WeekSchedule>,
    val settings: ScheduleSettings = ScheduleSettings()
)


@Serializable
data class ScheduleSettings(
    val defaultLessonDurationMinutes: Int = 90,
    val isWeekRotationEnabled: Boolean = true,
    val isFirstLaunch: Boolean = true,

    val scheduleType: ScheduleType = ScheduleType.Fixed,
    val semesterStartDate: String? = null
)