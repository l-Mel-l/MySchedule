package com.example.myschedule.notification

import com.example.myschedule.Lesson
import com.example.myschedule.MainSchedule
import com.example.myschedule.TimeUtils
import com.example.myschedule.activeWeeks
import com.example.myschedule.weekNumberFor
import java.time.LocalDate
import java.time.LocalDateTime

enum class SegmentKind {
    LESSON,
    BREAK
}

data class TimerSegment(
    val kind: SegmentKind,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val lesson: Lesson,
    val nextLesson: Lesson? = null,
    val isBeforeFirstLesson: Boolean = false
)

data class TimelineOptions(
    val preLessonMinutes: Int = 30,
    val includeBreaks: Boolean = true
)

data class TimelineResult(
    val current: TimerSegment?,
    val nextChange: LocalDateTime?
)

object TimerTimeline {

    private const val LOOKAHEAD_DAYS = 14

    private class Slot(val lesson: Lesson, val start: LocalDateTime, val end: LocalDateTime)

    fun segmentsForDate(
        schedule: MainSchedule,
        date: LocalDate,
        options: TimelineOptions
    ): List<TimerSegment> {
        val weekNumber = schedule.weekNumberFor(date)
        val day = schedule.activeWeeks
            .find { it.weekNumber == weekNumber }
            ?.days?.getOrNull(date.dayOfWeek.value - 1)
            ?: return emptyList()

        val slots = day.lessons
            .map { Slot(it, date.atTime(TimeUtils.parse(it.startTime)), date.atTime(TimeUtils.parse(it.endTime))) }
            .filter { it.end.isAfter(it.start) }
            .sortedBy { it.start }

        val result = mutableListOf<TimerSegment>()
        slots.forEachIndexed { index, slot ->
            val next = slots.getOrNull(index + 1)

            if (index == 0 && options.preLessonMinutes > 0) {
                val dayStart = date.atStartOfDay()
                val rawStart = slot.start.minusMinutes(options.preLessonMinutes.toLong())
                val windowStart = if (rawStart.isBefore(dayStart)) dayStart else rawStart
                if (windowStart.isBefore(slot.start)) {
                    result += TimerSegment(
                        kind = SegmentKind.BREAK,
                        start = windowStart,
                        end = slot.start,
                        lesson = slot.lesson,
                        isBeforeFirstLesson = true
                    )
                }
            }

            // Сама пара
            result += TimerSegment(
                kind = SegmentKind.LESSON,
                start = slot.start,
                end = slot.end,
                lesson = slot.lesson,
                nextLesson = next?.lesson
            )

            // Перемена до следующей пары
            if (next != null && options.includeBreaks && next.start.isAfter(slot.end)) {
                result += TimerSegment(
                    kind = SegmentKind.BREAK,
                    start = slot.end,
                    end = next.start,
                    lesson = next.lesson
                )
            }
        }
        return result
    }

    fun find(
        schedule: MainSchedule,
        now: LocalDateTime,
        options: TimelineOptions
    ): TimelineResult {
        val today = now.toLocalDate()
        for (offset in 0..LOOKAHEAD_DAYS) {
            val segments = segmentsForDate(schedule, today.plusDays(offset.toLong()), options)

            val current = segments.firstOrNull { !now.isBefore(it.start) && now.isBefore(it.end) }
            if (current != null) return TimelineResult(current, current.end)

            val upcoming = segments.filter { it.start.isAfter(now) }.minByOrNull { it.start }
            if (upcoming != null) return TimelineResult(null, upcoming.start)
        }
        return TimelineResult(null, null)
    }
}
