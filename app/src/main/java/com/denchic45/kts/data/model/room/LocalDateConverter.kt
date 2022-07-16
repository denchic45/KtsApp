package com.denchic45.kts.data.model.room

import androidx.room.TypeConverter
import com.denchic45.kts.util.*
import java.time.LocalDate

class LocalDateConverter {
    @TypeConverter
    fun toString(date: LocalDate): String {
        return date.toString(DatePatterns.yyy_MM_dd)
    }

    @TypeConverter
    fun toLocalDate(date: String): LocalDate {
        return date.toLocalDate(DatePatterns.yyy_MM_dd)
    }
}