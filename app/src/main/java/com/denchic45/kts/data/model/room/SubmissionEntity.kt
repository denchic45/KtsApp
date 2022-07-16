package com.denchic45.kts.data.model.room

import androidx.room.*
import com.denchic45.kts.domain.EntityModel
import com.denchic45.kts.data.model.domain.Task
import java.util.*

@Entity(
    tableName = "submission", foreignKeys = [
        ForeignKey(
            entity = CourseContentEntity::class,
            parentColumns = ["content_id"],
            childColumns = ["content_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["student_id"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
            deferred = true
        )
    ]
)
data class SubmissionEntity(
    @ColumnInfo(name = "submission_id")
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    @ColumnInfo(name = "student_id")
    val studentId: String,
    @ColumnInfo(name = "content_id")
    val contentId: String,
    @ColumnInfo(name = "course_id")
    val courseId: String,
    val status: Task.Submission.Status,
    @ColumnInfo(defaultValue = "")
    val text: String,
    @field:TypeConverters(ListConverter::class)
    val attachments: List<String>,
    @ColumnInfo(name = "grading_teacher_id")
    val teacherId: String?,
    @ColumnInfo(defaultValue = "")
    val cause: String,
    val grade: Int?,
    @field:TypeConverters(TimestampConverter::class)
    @ColumnInfo(name = "grade_date", defaultValue = "0")
    val gradedDate: Date?,
    @field:TypeConverters(TimestampConverter::class)
    @ColumnInfo(name = "rejected_date", defaultValue = "0")
    val rejectedDate: Date?,
    @field:TypeConverters(TimestampConverter::class)
    @ColumnInfo(name = "submitted_date", defaultValue = "0")
    val submittedDate: Date?,
    @field:TypeConverters(TimestampConverter::class)
    @ColumnInfo(name = "content_update_date", defaultValue = "0")
    val contentUpdateDate: Date?
) : EntityModel
