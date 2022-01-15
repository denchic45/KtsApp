package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.domain.ContentDetails
import com.denchic45.kts.data.model.domain.ContentType
import com.denchic45.kts.data.model.mapper.Default
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
    val completions: List<CompletionTaskDoc>? = emptyList(),
    @field:JvmField val deleted: Boolean = false,
    val contentType: ContentType
) : DocModel {
    private constructor() : this(
        "", "", "", "", "",
        false, emptyList(), 0, Date(), null, ContentDetails.Empty,
        Date(), emptyList(), contentType = ContentType.TASK
    )
}

data class CompletionTaskDoc(
    val studentId: String,
    val teacherId: String,
    val completedDate: Date,
    val grade: Int,
    val assessmentDate: Date,
)