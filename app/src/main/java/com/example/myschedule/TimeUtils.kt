package com.example.myschedule

import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields
import java.util.Locale

object TimeUtils {
    // Формат времени, который мы используем в json (HH:mm)
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    // Превращает строку "08:00" в объект времени
    fun parse(time: String): LocalTime {
        return try {
            LocalTime.parse(time, formatter)
        } catch (e: Exception) {
            LocalTime.MIN
        }
    }

    // Возвращает текущее время
    fun now(): LocalTime = LocalTime.now()

    // Считает, сколько секунд прошло от start до end
    fun secondsBetween(start: LocalTime, end: LocalTime): Long {
        return ChronoUnit.SECONDS.between(start, end)
    }

    // Форматирует секунды в красивое время "00:15:30"
    fun formatRemaining(seconds: Long): String {
        if (seconds < 0) return "00:00:00"
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    // Превращаем строку "2024-09-01" в дату
    fun parseDate(dateString: String?): LocalDate? {
        if (dateString == null) return null
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    // Сохраняем дату в строку
    fun formatDate(date: LocalDate): String {
        return date.toString() // формат ISO "YYYY-MM-DD"
    }

    // Главная магия: Считаем номер текущей недели
    // Если семестр еще не начался -> вернем 1
    // Если семестр идет -> вернем реальный номер (1, 2, 3...)
    fun getCurrentWeekNumber(startDateStr: String?): Int {
        val startDate = parseDate(startDateStr) ?: return 1
        val today = LocalDate.now()

        // Если дата в будущем (как у тебя 2025 год) - возвращаем 1
        if (today.isBefore(startDate)) return 1

        // Тупая математика: (Количество дней / 7) + 1
        val daysPassed = ChronoUnit.DAYS.between(startDate, today)
        return (daysPassed / 7).toInt() + 1
    }
}