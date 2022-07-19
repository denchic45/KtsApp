package com.denchic45.kts.data.local.db

import com.denchic45.kts.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubmissionCommentLocalDataSource(db: AppDatabase) {

    private val queries: SubmissionCommentEntityQueries = db.submissionCommentEntityQueries

    suspend fun upsert(submissionCommentEntity: SubmissionCommentEntity) = withContext(Dispatchers.IO) {
        queries.upsert(submissionCommentEntity)
    }

    suspend fun upsert(submissionCommentEntities: List<SubmissionCommentEntity>) = withContext(Dispatchers.IO) {
        queries.transaction {
           submissionCommentEntities.forEach { queries.upsert(it) }
        }
    }
}