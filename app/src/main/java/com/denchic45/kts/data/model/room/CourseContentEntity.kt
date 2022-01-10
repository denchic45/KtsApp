package com.denchic45.kts.data.model.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.EntityModel
import com.denchic45.kts.data.model.domain.ContentType
import java.util.*

@Entity(tableName = "course_content")
data class CourseContentEntity(
    @field:PrimaryKey
    @field:ColumnInfo(name = "content_id")
    var id: String,
    var courseId: String,
    val sectionId: String,
    val name: String,
    @field:TypeConverters(ListConverter::class) val attachments: List<String>,
    val description: String,
    val details: String,
    val commentsEnabled: Boolean,
    val contentType: ContentType,
    @field:TypeConverters(DateConverter::class)
    @field:ColumnInfo(name = "date_created")
    val createdDate: Date,
    @field:TypeConverters(TimestampConverter::class)
    val timestamp: Date,
    @field:TypeConverters(TimestampConverter::class)
    @field:ColumnInfo(name = "date_completion")
    val completionDate: Date
) : EntityModel