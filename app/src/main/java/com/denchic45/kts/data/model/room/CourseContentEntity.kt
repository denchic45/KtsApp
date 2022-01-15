package com.denchic45.kts.data.model.room

import androidx.room.*
import com.denchic45.kts.data.model.EntityModel
import com.denchic45.kts.data.model.domain.ContentType
import com.denchic45.kts.data.model.mapper.Default
import java.util.*

@Entity(tableName = "course_content")
data class CourseContentEntity @Default @JvmOverloads constructor(
    @field:PrimaryKey
    @field:ColumnInfo(name = "content_id")
    var id: String,
    @field:ColumnInfo(name = "course_id")
    var courseId: String,
    @field:ColumnInfo(name = "section_id")
    val sectionId: String,
    val name: String,
    @field:TypeConverters(ListConverter::class) val attachments: List<String>,
    val description: String,
    val details: String,
    val commentsEnabled: Boolean,
    val contentType: ContentType,
    @field:TypeConverters(DateConverter::class)
    @field:ColumnInfo(name = "created_date")
    val createdDate: Date,
    @field:TypeConverters(TimestampConverter::class)
    val timestamp: Date,
    @field:TypeConverters(TimestampConverter::class)
    @field:ColumnInfo(name = "completion_date")
    val completionDate: Date?,
    @Ignore var deleted: Boolean = false
) : EntityModel