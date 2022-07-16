package com.denchic45.kts.util

import java.text.SimpleDateFormat
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

fun String.toDate(format: String): Date = SimpleDateFormat(format, Locale.getDefault()).parse(this)!!

fun Date.toString(format: String): String = SimpleDateFormat(format, Locale.getDefault()).format(this)

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
            val sdf = SimpleDateFormat(DatePatterns.LLLL, Locale.getDefault())
            sdf.format(date.toDate())
        } else {
            val sdf = SimpleDateFormat(DatePatterns.LLLL_yyyy, Locale.getDefault())
            return sdf.format(date.toDate())
        }
    }

    fun isValidDate(date: String, pattern: String): Boolean {
        return try {
            date.toLocalDate(pattern)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }
}

object DatePatterns {
    const val yyy_MM_dd = "yyyy-MM-dd"
    const val LLLL = "LLLL"
    const val LLLL_yyyy = "LLLL yyyy"
    const val DD_MM_yy = "dd.MM.yy"
    const val dd_MMM = "dd MMM"
    const val dd_MMMM = "dd MMMM"
}