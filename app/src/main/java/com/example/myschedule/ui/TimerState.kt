package com.example.myschedule.ui

import com.example.myschedule.Lesson

sealed class StudentState {
    object Loading : StudentState()
    // Добавили nextLesson (может быть null, если это последняя пара)
    data class LessonNow(
        val lesson: Lesson,
        val nextLesson: Lesson?,
        val progress: Float,
        val secondsLeft: Long
    ) : StudentState()

    // Добавили progress для перемены
    data class BreakNow(
        val nextLesson: Lesson,
        val progress: Float,
        val secondsLeft: Long
    ) : StudentState()

    data class DayFinished(val nextDayFirstLesson: Lesson? = null) : StudentState()
    object FreeDay : StudentState()
}