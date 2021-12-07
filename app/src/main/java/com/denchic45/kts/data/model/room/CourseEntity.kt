package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.denchic45.kts.data.model.EntityModel

@Entity(
    tableName = "course",
    foreignKeys = [ForeignKey(
        entity = SubjectEntity::class,
        parentColumns = ["uuid_subject"],
        childColumns = ["uuid_subject"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["uuid_user"],
        childColumns = ["uuid_teacher"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )]
)
class CourseEntity(
    @field:PrimaryKey
    @field:ColumnInfo(name = "uuid_course")
    var uuid: String,
    var name: String,
    @field:ColumnInfo(index = true, name = "uuid_subject")
    var subjectUuid: String,
    @field:ColumnInfo(index = true, name = "uuid_teacher")
    var teacherUuid: String
) : EntityModel