package com.denchic45.kts.data.remote.model

import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.domain.model.ContentDetails
import com.denchic45.kts.domain.model.ContentType
import com.denchic45.kts.domain.model.Task
import java.util.*

data class CourseContentDoc constructor(
    val id: String,
    val courseId: String = "",
    val sectionId: String = "",
    val name: String,
    val description: String,
    val commentsEnabled: Boolean,
    var attachments: List<String>,
    var order: Long,
    val createdDate: Date,
    val timestamp: Date?,
    val comments: List<ContentCommentDoc>?,
    val submissions: Map<String, SubmissionDoc>?,
    @field:JvmField
    val deleted: Boolean = false,
    val contentType: ContentType,
    val completionDate: Date?,
    val weekDate: String?,
    val contentDetails: ContentDetails
) {
    private constructor() : this(
        "",
        "",
        "",
        "",
        "",
        false,
        emptyList(),
        0,
        Date(),
        null,
        emptyList(),
        emptyMap(),
        false,
        contentType = ContentType.TASK,
        null,
        null,
        ContentDetails.Empty
    )
}

data class SubmissionDoc(
    val id: String,
    val studentId: String,
    val contentId: String,
    val courseId: String,
    val status: Task.Submission.Status,
    val text: String,
    val attachments: List<String>,
    val teacherId: String,
    val grade: Int,
    val gradedDate: Date,
    val timestamp: Date,
    val rejectedDate: Date,
    val cause: String,
    val comments: List<SubmissionCommentDoc>,
    val submittedDate: Date,
) {
    private constructor(
    ) : this(
        "", "", "", "",
        Task.Submission.Status.NOT_SUBMITTED,
        "",
        emptyList(),
        "",
        0,
        Date(),
        Date(),
        Date(),
        "",
        emptyList(), Date()
    )

    companion object {
        fun createNotSubmitted(
            studentId: String,
            contentId: String,
            courseId: String
        ): SubmissionDoc {
            return SubmissionDoc(studentId, contentId, courseId)
        }
    }

    constructor(studentId: String, contentId: String, courseId: String) : this(
        "",
        studentId,
        contentId,
        courseId,
        Task.Submission.Status.NOT_SUBMITTED,
        "",
        emptyList(),
        "",
        0,
        Date(),
        Date(),
        Date(),
        "",
        emptyList(), Date()
    )
}