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
    val completionDate: Date,
    val createdDate: Date,
    val timestamp: Date,
    @get:PropertyName("details")
    val contentDetails: ContentDetails,
    val contentType: ContentType,
    val completions: List<CompletionTask>? = emptyList()
) : DocModel {
    private constructor() : this(
        "", "", "", "", "",
        false, emptyList(), Date(), Date(), Date(), ContentDetails.Empty,
        ContentType.TASK, emptyList()
    )
}

data class CompletionTask(
    val studentUuid: String,
    val teacherUuid: String,
    val completedDate: Date,
    val grade: Int,
    val assessmentDate: Date,
)