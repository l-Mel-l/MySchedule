package com.example.myschedule

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.LocalDate

object TimeUtils {
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    fun parse(time: String): LocalTime {
        return try {
            LocalTime.parse(time, formatter)
        } catch (e: Exception) {
            LocalTime.MIN
        }
    }

    fun now(): LocalTime = LocalTime.now()

    fun secondsBetween(start: LocalTime, end: LocalTime): Long {
        return ChronoUnit.SECONDS.between(start, end)
    }

    fun formatRemaining(seconds: Long): String {
        if (seconds < 0) return "00:00:00"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun parseDate(dateString: String?): LocalDate? {
        if (dateString == null) return null
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    fun formatDate(date: LocalDate): String {
        return date.toString()
    }

    fun getCurrentWeekNumber(startDateStr: String?): Int {
        val startDate = parseDate(startDateStr) ?: return 1
        val today = LocalDate.now()

        if (today.isBefore(startDate)) return 1

        val daysPassed = ChronoUnit.DAYS.between(startDate, today)
        return (daysPassed / 7).toInt() + 1
    }
}