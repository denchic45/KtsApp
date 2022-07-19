package com.denchic45.kts.data.remote.model

import java.util.*

class ContentCommentDoc(
    val id: String,
    val contentId: String,
    val content: String,
    val authorId: String,
    val createdDate: Date
)
