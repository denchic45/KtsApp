package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.denchic45.kts.domain.EntityModel

@Entity(
    tableName = "course",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["user_id"],
        childColumns = ["teacher_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
class CourseEntity(
    @field:PrimaryKey
    @field:ColumnInfo(name = "course_id")
    var id: String,
    var name: String,
    @field:ColumnInfo(index = true, name = "subject_id")
    var subjectId: String,
    @field:ColumnInfo(index = true, name = "teacher_id")
    var teacherId: String
) : EntityModel