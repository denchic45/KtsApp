package com.denchic45.kts.data.model.room

import androidx.room.TypeConverter

object ListConverter {
    @TypeConverter
    fun fromList(list: List<String>): String {
        return list.joinToString { "," }
    }

    @TypeConverter
    fun tolList(s: String): List<String> {
        return listOf(*s.split(",".toRegex()).toTypedArray())
    }
}