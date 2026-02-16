package com.example.myschedule.ui

import com.example.myschedule.Lesson

sealed class StudentState {
    object Loading : StudentState()
    data class LessonNow(
        val lesson: Lesson,
        val nextLesson: Lesson?,
        val progress: Float,
        val secondsLeft: Long
    ) : StudentState()

    data class BreakNow(
        val nextLesson: Lesson,
        val progress: Float,
        val secondsLeft: Long
    ) : StudentState()

    data class DayFinished(val nextDayFirstLesson: Lesson? = null) : StudentState()
    object FreeDay : StudentState()
}