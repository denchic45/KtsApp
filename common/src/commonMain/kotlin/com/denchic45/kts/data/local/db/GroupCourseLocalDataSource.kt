package com.denchic45.kts.data.local.db

import com.denchic45.kts.AppDatabase
import com.denchic45.kts.GroupCourseEntity
import com.denchic45.kts.GroupCourseEntityQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GroupCourseLocalDataSource (db:AppDatabase) {
    private val queries: GroupCourseEntityQueries = db.groupCourseEntityQueries

    suspend fun upsert(groupCourseEntity: GroupCourseEntity) = withContext(Dispatchers.IO) {
        queries.upsert(groupCourseEntity)
    }

    suspend fun upsert(groupCourseEntities: List<GroupCourseEntity>) = withContext(Dispatchers.IO) {
        queries.transaction {
            groupCourseEntities.forEach { queries.upsert(it) }
        }
    }

     suspend fun deleteByGroup(groupId: String) = withContext(Dispatchers.IO) {
         queries.deleteByGroup(groupId)
    }

     suspend fun deleteByCourse(courseId: String) = withContext(Dispatchers.IO) {
        queries.deleteByCourse(courseId)
    }
}