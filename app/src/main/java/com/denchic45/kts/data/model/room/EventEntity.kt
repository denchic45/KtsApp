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
    @ColumnInfo(name = "eventUuid")
    var uuid: String = UUID.randomUUID().toString(),

    @field:TypeConverters(TimestampConverter::class)
    var timestamp: Date? = null,
    var order: Int = 0,

    @field:TypeConverters(DateConverter::class)
    var date: Date? = null,
    var room: String? = null,

    @ColumnInfo(name = "uuid_subject")
    var subjectUuid: String? = null,

    @field:TypeConverters(ListConverter::class)
    var teacherUuidList: List<String>? = null,
    var groupUuid: String? = null,
    var type: TYPE? = null,
    var name: String? = null,
    var iconUrl: String? = null,
    var color: String? = null,
) : EntityModel {
    enum class TYPE {
        LESSON, SIMPLE, EMPTY
    }
}

