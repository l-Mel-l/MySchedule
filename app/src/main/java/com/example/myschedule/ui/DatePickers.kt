package com.example.myschedule.ui

import android.app.DatePickerDialog
import android.content.Context
import java.time.LocalDate

fun showSemesterDatePicker(context: Context, current: LocalDate?, onPicked: (LocalDate) -> Unit) {
    val initial = current ?: LocalDate.now()
    DatePickerDialog(
        context,
        { _, year, month, day -> onPicked(LocalDate.of(year, month + 1, day)) },
        initial.year,
        initial.monthValue - 1,
        initial.dayOfMonth
    ).show()
}
