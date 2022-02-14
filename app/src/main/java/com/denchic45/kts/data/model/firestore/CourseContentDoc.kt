package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.domain.ContentDetails
import com.denchic45.kts.data.model.domain.ContentType
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.mapper.Default
import com.denchic45.kts.data.model.room.ContentCommentEntity
import com.denchic45.kts.data.model.room.SubmissionCommentEntity
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

@IgnoreExtraProperties
data class CourseContentDoc @Default @JvmOverloads constructor(
    val id: String,
    val courseId: String = "",
    val sectionId: String = "",
    val name: String,
    val description: String,
    val commentsEnabled: Boolean,
    var attachments: List<String>,
    var order: Long,
    val createdDate: Date,
    @ServerTimestamp
    val timestamp: Date?,
    val comments: List<ContentCommentEntity>?,
    val submissions: Map<String, SubmissionDoc>?,
    @field:JvmField
    val deleted: Boolean = false,
    val contentType: ContentType,
    val completionDate: Date?,
    @get:Exclude
    val contentDetails: ContentDetails
) : DocModel {
    private constructor() : this(
        "", "", "", "", "",
        false, emptyList(), 0, Date(), null,
        emptyList(), emptyMap(), false, contentType = ContentType.TASK, null, ContentDetails.Empty
    )
}

data class SubmissionDoc(
    val studentId: String,
    val contentId: String,
    val courseId: String,
    val status: Task.Submission.Status,
    val text: String,
    val attachments: List<String>,
    val teacherId: String,
    val grade: Int,
    val gradedDate: Date,
    val contentUpdateDate: Date,
    val rejectedDate: Date,
    val cause: String,
    val comments: List<SubmissionCommentEntity>,
    val submittedDate: Date,
) {
    private constructor() : this(
        "", "",
        "",
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