package com.denchic45.kts.data.remote.model

import java.util.*

class SubmissionCommentDoc(
    val id: String,
    val submissionId: String,
    val content: String,
    val authorId: String,
    val createdDate: Date
)