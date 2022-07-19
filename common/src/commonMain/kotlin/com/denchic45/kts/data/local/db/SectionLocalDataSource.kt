package com.denchic45.kts.data.local.db

import com.denchic45.kts.AppDatabase
import com.denchic45.kts.SectionEntity
import com.denchic45.kts.SectionEntityQueries
import com.denchic45.kts.data.mapper.ListMapper
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SectionLocalDataSource(db: AppDatabase) {

    private val queries: SectionEntityQueries = db.sectionEntityQueries

    suspend fun upsert(sectionEntity: SectionEntity) = withContext(Dispatchers.IO) {
        queries.upsert(sectionEntity)
    }

    suspend fun upsert(sectionEntities: List<SectionEntity>) = withContext(Dispatchers.IO) {
        queries.transaction {
            sectionEntities.forEach { queries.upsert(it) }
        }
    }

    suspend fun get(sectionId: String): SectionEntity? = withContext(Dispatchers.IO) {
        queries.getById(sectionId).executeAsOneOrNull()
    }

    fun getByCourseId(courseId: String): Flow<List<SectionEntity>> {
        return queries.getByCourseId(courseId).asFlow().mapToList()
    }

    suspend fun deleteByCourseId(courseId: String) = withContext(Dispatchers.IO) {
        queries.deleteByCourseId(courseId)
    }


//    suspend fun deleteMissing(availableSections: List<String>) = withContext(Dispatchers.IO) {
//        queries.deleteMissing(ListMapper.fromList(availableSections))
//    }
}