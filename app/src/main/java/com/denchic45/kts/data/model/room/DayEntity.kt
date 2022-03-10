package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate
import java.util.*

@Entity(tableName = "day")
data class DayEntity(
    @PrimaryKey
    @ColumnInfo(name = "day_id")
    var id: String,
    @field:TypeConverters(LocalDateConverter::class)
    var date: LocalDate,
    @ColumnInfo(name = "group_id")
    var groupId: String
)