package com.denchic45.kts.data.model.room

import androidx.room.TypeConverter
import com.denchic45.kts.utils.DateFormatUtil
import com.denchic45.kts.utils.toLocalDate
import com.denchic45.kts.utils.toString
import java.time.LocalDate

class LocalDateConverter {
    @TypeConverter
    fun toString(date: LocalDate): String {
        return date.toString(DateFormatUtil.yyy_MM_dd)
    }

    @TypeConverter
    fun toLocalDate(date: String): LocalDate {
        return date.toLocalDate(DateFormatUtil.yyy_MM_dd)
    }
}