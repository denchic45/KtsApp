package com.denchic45.kts.data.local.db

import com.denchic45.kts.AppDatabase
import com.denchic45.kts.SubjectEntity
import com.denchic45.kts.SubjectEntityQueries
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SubjectLocalDataSource(db: AppDatabase) {
    private val queries: SubjectEntityQueries = db.subjectEntityQueries

    suspend fun upsert(subjectEntity: SubjectEntity) = withContext(Dispatchers.IO) {
        queries.upsert(subjectEntity)
    }

    suspend fun upsert(subjectEntities: List<SubjectEntity>) = withContext(Dispatchers.IO) {
        queries.transaction {
            subjectEntities.forEach { queries.upsert(it) }
        }
    }

    suspend fun get(subjectId: String): SubjectEntity? = withContext(Dispatchers.IO) {
        queries.getById(subjectId).executeAsOneOrNull()
    }

    fun observe(id: String): Flow<SubjectEntity?> {
        return queries.getById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    fun observeByGroupId(groupId: String): Flow<List<SubjectEntity>> {
        return queries.getByGroupId(groupId).asFlow().mapToList(Dispatchers.IO)
    }
}