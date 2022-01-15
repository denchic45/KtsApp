package com.denchic45.kts.data.model.room

import androidx.room.TypeConverter
import java.util.*

class TimestampConverter {
    @TypeConverter
    fun toLong(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(timestamp: Long?): Date? = timestamp?.let { Date(it) }

}