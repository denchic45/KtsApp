package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.denchic45.kts.data.model.EntityModel

@Entity(
    tableName = "teacher_event",
    primaryKeys = ["event_id", "user_id"],
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["user_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    ), ForeignKey(
        entity = EventEntity::class,
        parentColumns = ["event_id"],
        childColumns = ["event_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
class TeacherEventCrossRef(
    @field:ColumnInfo(index = true, name = "event_id")
    var eventId: String,
    @field:ColumnInfo(index = true, name = "user_id")
    var teacherId: String
) : EntityModel