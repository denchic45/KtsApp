package com.denchic45.kts.data.model.room

import androidx.room.TypeConverter
import com.denchic45.kts.utils.*
import java.time.LocalDate
import java.time.LocalDateTime

class LocalDateTimeConverter {
    @TypeConverter
    fun toString(date: LocalDateTime): String {
        return date.toString(DatePatterns.yyy_MM_dd)
    }

    @TypeConverter
    fun toLocalDate(date: String): LocalDateTime {
        return date.toLocalDateTime(DatePatterns.yyy_MM_dd)
    }
}