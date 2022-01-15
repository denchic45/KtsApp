package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.EntityModel
import java.util.*

@Entity(tableName = "event")
data class EventEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_id")
    var id: String,
    @field:TypeConverters(TimestampConverter::class)
    var timestamp: Date,
    var order: Int = 0,
    @field:TypeConverters(DateConverter::class)
    var date: Date,
    var room: String,
    @ColumnInfo(name = "subject_id")
    var subjectId: String,
    @field:TypeConverters(ListConverter::class)
    @ColumnInfo(name = "teacher_ids")
    var teacherIds: List<String>,
    @ColumnInfo(name = "group_id")
    var groupId: String,
    var type: TYPE,
    var name: String,
    @ColumnInfo(name = "icon_url")
    var iconUrl: String,
    var color: String,
) : EntityModel {
    enum class TYPE {
        LESSON, SIMPLE, EMPTY
    }
}

