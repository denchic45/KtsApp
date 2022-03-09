package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.denchic45.kts.data.model.EntityModel

@Entity(
    tableName = "group_course",
    primaryKeys = ["group_id", "course_id"],
    foreignKeys = [ForeignKey(
        entity = GroupEntity::class,
        parentColumns = ["group_id"],
        childColumns = ["group_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        deferred = true
    ), ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["course_id"],
        childColumns = ["course_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
        deferred = true
    )]
)
class GroupCourseCrossRef(
    @field:ColumnInfo(index = true, name = "group_id")
    val groupId: String, @field:ColumnInfo(index = true, name = "course_id") val courseId: String
) : EntityModel