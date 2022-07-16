package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.denchic45.kts.domain.EntityModel

@Entity(tableName = "section", foreignKeys = [
    ForeignKey(
        entity = CourseEntity::class,
        parentColumns = ["course_id"],
        childColumns = ["course_id"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE
    )
])
data class SectionEntity(
    @PrimaryKey
    @ColumnInfo(name = "section_id")
    val id: String,
    @ColumnInfo(name = "course_id")
    val courseId: String,
    val name: String,
    val order: Int
): EntityModel
