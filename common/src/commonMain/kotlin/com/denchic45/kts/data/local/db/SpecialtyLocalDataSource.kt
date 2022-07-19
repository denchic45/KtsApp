package com.denchic45.kts.data.local.db

import com.denchic45.kts.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpecialtyLocalDataSource(db: AppDatabase) {

    private val queries: SpecialtyEntityQueries = db.specialtyEntityQueries

    suspend fun upsert(specialtyEntity: SpecialtyEntity) = withContext(Dispatchers.IO) {
        queries.upsert(specialtyEntity)
    }

    suspend fun upsert(specialtyEntities: List<SpecialtyEntity>) = withContext(Dispatchers.IO) {
        queries.transaction {
           specialtyEntities.forEach {
               queries.upsert(it)
           }
        }
    }
}