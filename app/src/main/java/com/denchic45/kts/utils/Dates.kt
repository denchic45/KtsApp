package com.denchic45.kts.utils

import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*

fun LocalDateTime.toString(pattern: String): String =
    DateTimeFormatter.ofPattern(pattern).format(this)

fun LocalDate.toString(pattern: String): String =
    DateTimeFormatter.ofPattern(pattern).format(this)

fun String.toLocalDateTime(pattern: String): LocalDateTime =
    LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))

fun Date.toLocalDateTime(): LocalDateTime =
    LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

fun String.toLocalDate(pattern: String): LocalDate =
    LocalDate.parse(this, DateTimeFormatter.ofPattern(pattern))

fun Date.toLocalDate(): LocalDate =
    toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

fun LocalDateTime.toDate(): Date = Date.from(atZone(ZoneId.systemDefault()).toInstant())

fun LocalDate.toDate(): Date = Date.from(
    atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant()
)

fun LocalDate.toDateUTC(): Date = Date.from(
    atStartOfDay()
        .atOffset(ZoneOffset.UTC)
        .toInstant()
)

object Dates {
    fun toStringHidingCurrentYear(date: LocalDate): String {
        return if (Year.now().value == date.year) {
            val sdf = SimpleDateFormat(DateFormatUtil.LLLL, Locale.getDefault())
            sdf.format(date.toDate())
        } else {
            val sdf = SimpleDateFormat(DateFormatUtil.LLLL_yyyy, Locale.getDefault())
            return sdf.format(date.toDate())
        }
    }
}