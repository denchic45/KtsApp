package com.denchic45.kts.data.model.room

import androidx.room.TypeConverter
import com.denchic45.kts.utils.*
import java.util.*

class DateConverter {
    @TypeConverter
    fun toString(date: Date): String {
        return date.toString(DatePatterns.yyy_MM_dd)
    }

    @TypeConverter
    fun toDate(date: String): Date {
        return date.toDate(DatePatterns.yyy_MM_dd)
    }
}