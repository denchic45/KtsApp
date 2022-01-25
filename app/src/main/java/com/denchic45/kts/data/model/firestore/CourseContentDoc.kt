package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.domain.ContentDetails
import com.denchic45.kts.data.model.domain.ContentType
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.mapper.Default
import com.denchic45.kts.data.model.room.ContentCommentEntity
import com.denchic45.kts.data.model.room.SubmissionCommentEntity
import com.google.firebase.firestore.PropertyName
import java.util.*

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
    val completionDate: Date?,
    @get:PropertyName("details")
    val contentDetails: ContentDetails,
    val timestamp: Date,
    val comments: List<ContentCommentEntity>?,
    val submissions: List<SubmissionDoc>?,
    @field:JvmField val deleted: Boolean = false,
    val contentType: ContentType
) : DocModel {
    private constructor() : this(
        "", "", "", "", "",
        false, emptyList(), 0, Date(), null, ContentDetails.Empty,
        Date(), emptyList(), emptyList(), contentType = ContentType.TASK
    )
}

data class SubmissionDoc(
    val studentId: String,
    val taskId: String,
    val courseId: String,
    val status: Task.Submission.Status,
    val text: String,
    val attachments: List<String>,
    val teacherId: String,
    val grade: Int,
    val gradedDate: Date,
    val comments: List<SubmissionCommentEntity>,
    val doneDate: Date,
) {
    private constructor() : this(
        "", "",
        "", Task.Submission.Status.NOTHING, "",
        emptyList(), "", 0,
        Date(),
        emptyList(), Date()
    )
}