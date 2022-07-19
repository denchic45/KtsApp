package com.denchic45.kts.data.local.db

import com.denchic45.kts.AppDatabase
import com.denchic45.kts.GetStudentsWithCuratorByGroupId
import com.denchic45.kts.UserEntity
import com.denchic45.kts.UserEntityQueries
import com.denchic45.kts.data.mapper.ListMapper
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import com.squareup.sqldelight.runtime.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withContext

class UserLocalDataSource(db: AppDatabase) {

    private val queries: UserEntityQueries = db.userEntityQueries

    suspend fun upsert(userEntity: UserEntity) = withContext(Dispatchers.Default) {
        queries.upsert(userEntity)
    }

    suspend fun upsert(userEntities: List<UserEntity>) {
        queries.transaction {
            userEntities.forEach { queries.upsert(it) }
        }
    }

    suspend fun get(id: String): UserEntity? = withContext(Dispatchers.Default) {
        queries.getById(id).executeAsOneOrNull()
    }

    suspend fun getAll(): List<UserEntity> = withContext(Dispatchers.Default) {
        queries.getAll().executeAsList()
    }

    fun observe(id: String): Flow<UserEntity?> {
        return queries.getById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    fun observeByGroupId(groupId: String): Flow<List<UserEntity>> {
        return queries.getByGroupId(groupId).asFlow().mapToList()
    }

    suspend fun isExist(id: String) = withContext(Dispatchers.IO) {
        queries.isExist(id).executeAsOne()
    }

    suspend fun clearTeachers() = withContext(Dispatchers.IO) {
        queries.clearTeachers()
    }

    suspend fun deleteMissingStudentsByGroup(groupId: String, availableStudents: List<String>) =
        withContext(Dispatchers.IO) {
            queries.deleteMissingStudentsByGroup(groupId, ListMapper.fromList(availableStudents))
        }

    suspend fun getGroupId(userId: String): String = withContext(Dispatchers.IO) {
        queries.getGroupId(userId).executeAsOne()
    }

    suspend fun isExistByIdAndGroupId(id: String, groupId: String): Boolean =
        withContext(Dispatchers.IO) {
            queries.isExistByIdAndGroupId(id, groupId).executeAsOne()
        }

    suspend fun getStudentIdsOfCourseByCourseId(courseId: String): List<String> =
        withContext(Dispatchers.IO) {
            queries.getStudentIdsOfCourseByCourseId(courseId).executeAsList()
        }

    fun observeStudentsWithCuratorByGroupId(groupId: String): Flow<List<GetStudentsWithCuratorByGroupId>> {
        return queries.getStudentsWithCuratorByGroupId(groupId)
            .asFlow()
            .mapToList()

    }
}