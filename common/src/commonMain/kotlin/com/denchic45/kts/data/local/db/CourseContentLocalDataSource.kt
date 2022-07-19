package com.denchic45.kts.data.local.db

import com.denchic45.kts.AppDatabase
import com.denchic45.kts.CourseContentEntity
import com.denchic45.kts.CourseContentEntityQueries
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.*

class CourseContentLocalDataSource(db: AppDatabase) {

    private val queries: CourseContentEntityQueries = db.courseContentEntityQueries

    suspend fun upsert(courseContentEntity: CourseContentEntity) = withContext(Dispatchers.IO) {
        queries.upsert(courseContentEntity)
    }

    fun getByCourseId(courseId: String): Flow<List<CourseContentEntity>> {
        return queries.getByCourseId(courseId).asFlow().mapToList(Dispatchers.IO)
    }

    fun observe(id: String): Flow<CourseContentEntity?> {
        return queries.getById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    suspend fun get(id: String): CourseContentEntity? = withContext(Dispatchers.IO) {
        queries.getById(id).executeAsOneOrNull()
    }

    fun getAttachmentsById(id: String): Flow<List<String>> {
        return queries.getAttachmentsById(id).asFlow().mapToOne(Dispatchers.IO)
    }

    suspend fun getCourseIdByTaskId(taskId: String) = withContext(Dispatchers.IO) {
        queries.getCourseIdByTaskId(taskId).executeAsOne()
    }


    fun getByGroupIdAndGreaterCompletionDate(
        groupId: String,
        startDate: Date = Date()
    ): Flow<List<CourseContentEntity>> {
        return queries.getByGroupIdAndGreaterCompletionDate(startDate.time, groupId)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    fun getByGroupIdAndNotSubmittedUser(
        groupId: String,
        studentId: String,
        endDate: Date = Date(),
    ): Flow<List<CourseContentEntity>> {
        return queries.getByGroupIdAndNotSubmittedUser(endDate.time, groupId, studentId)
            .asFlow()
            .mapToList(Dispatchers.IO)
    }

    fun getByGroupIdAndSubmittedUser(
        groupId: String,
        studentId: String,
    ): Flow<List<CourseContentEntity>> {
        return queries.getByGroupIdAndSubmittedUser(groupId, studentId).asFlow().mapToList(Dispatchers.IO)
    }
}