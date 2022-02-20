package com.denchic45.kts.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
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

fun LocalDateTime.toDate(): Date = Date.from(atZone(ZoneId.systemDefault()).toInstant())

fun LocalDate.toDate(): Date = Date.from(
    atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant()
)