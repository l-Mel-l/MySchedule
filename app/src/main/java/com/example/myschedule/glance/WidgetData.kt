package com.example.myschedule.glance

import android.content.Context
import com.example.myschedule.Lesson
import com.example.myschedule.ScheduleRepository
import com.example.myschedule.ScheduleType
import com.example.myschedule.activeWeeks
import com.example.myschedule.displayName
import com.example.myschedule.notification.TimelineOptions
import com.example.myschedule.notification.TimerTimeline
import com.example.myschedule.notification.SegmentKind
import com.example.myschedule.weekNumberFor
import java.time.LocalDateTime
import java.time.ZoneId

enum class WidgetKind {
    LESSON,
    BREAK,
    FINISHED,
    FREE
}

class WidgetSnapshot(
    val kind: WidgetKind,
    val lesson: Lesson?,
    val nextLesson: Lesson?,
    val endMillis: Long,
    val beforeFirst: Boolean,
    val dayTitle: String,
    val lessons: List<Lesson>,
    val now: LocalDateTime
)

suspend fun loadWidgetSnapshot(context: Context): WidgetSnapshot {
    val now = LocalDateTime.now()
    val schedule = ScheduleRepository(context).loadSchedule()
        ?: return WidgetSnapshot(WidgetKind.FREE, null, null, 0L, false, "Сегодня", emptyList(), now)

    val date = now.toLocalDate()
    val weekNumber = schedule.weekNumberFor(date)
    val week = schedule.activeWeeks.find { it.weekNumber == weekNumber }
    val day = week?.days?.getOrNull(date.dayOfWeek.value - 1)
    val lessons = day?.lessons?.sortedBy { it.startTime } ?: emptyList()

    val dayName = day?.dayName ?: "Сегодня"
    val title = if (schedule.settings.scheduleType == ScheduleType.Fixed || week == null) dayName
    else "$dayName · ${week.displayName()}"

    val options = TimelineOptions(preLessonMinutes = 24 * 60, includeBreaks = true)
    val segment = TimerTimeline.find(schedule, now, options).current

    if (segment != null) {
        val endMillis = segment.end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        return when (segment.kind) {
            SegmentKind.LESSON -> WidgetSnapshot(
                WidgetKind.LESSON, segment.lesson, segment.nextLesson, endMillis, false, title, lessons, now
            )
            SegmentKind.BREAK -> WidgetSnapshot(
                WidgetKind.BREAK, segment.lesson, null, endMillis, segment.isBeforeFirstLesson, title, lessons, now
            )
        }
    }

    val hadLessonsToday = TimerTimeline.segmentsForDate(schedule, date, options).isNotEmpty()
    val kind = if (hadLessonsToday) WidgetKind.FINISHED else WidgetKind.FREE
    return WidgetSnapshot(kind, null, null, 0L, false, title, lessons, now)
}
