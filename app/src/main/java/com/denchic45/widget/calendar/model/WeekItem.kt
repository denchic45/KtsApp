package com.denchic45.widget.calendar.model

import java.time.LocalDate

class WeekItem(
    monday: LocalDate,
    var selectedDay: Int = -1
) {
    val daysOfWeek: List<LocalDate> = List(7) { monday.plusDays(it.toLong()) }

    operator fun get(position: Int): LocalDate {
        return daysOfWeek[position]
    }

    fun findAndSetCurrentDay() {
        selectedDay = findToday()
    }

    private fun findToday(): Int {
        return daysOfWeek.indexOfFirst { it == LocalDate.now() }
    }
}