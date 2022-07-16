package com.denchic45.kts.data.db

import com.denchic45.kts.AppDatabase
import com.denchic45.kts.UserEntity
import com.denchic45.kts.UserEntityQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class UserLocalDataSourceImpl(
    db: AppDatabase
) : UserLocalDataSource {

    private val queries: UserEntityQueries = db.userEntityQueries

    override suspend fun upsert(userEntity: UserEntity) = withContext(Dispatchers.Default) {
        queries.upsert(userEntity)
    }

    override suspend fun upsert(userEntities: List<UserEntity>) {
        queries.transaction {
            userEntities.forEach {
                queries.upsert(it)
            }
        }
    }

    override suspend fun get(id: String): UserEntity? = withContext(Dispatchers.Default) {
        queries.getById(id).executeAsOneOrNull()
    }

    override suspend fun getAll(): List<UserEntity> = withContext(Dispatchers.Default) {
        queries.getAll().executeAsList()
    }


    override fun observe(id: String): Flow<UserEntity?> {
        TODO("Not yet implemented")
    }

    override fun observeByGroupId(groupId: String): Flow<List<UserEntity>> {
        TODO("Not yet implemented")
    }

    override suspend fun isExist(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun clearTeachers() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMissingTeachersByGroup(
        availableUsers: List<String>,
        groupId: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteById(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun updateGroupId(userId: String, groupId: String?) {
        TODO("Not yet implemented")
    }

    override fun observeCurator(groupId: String): Flow<UserEntity?> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurator(groupId: String): UserEntity {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupId(userId: String): String {
        TODO("Not yet implemented")
    }


    override suspend fun isExistByIdAndGroupId(id: String, groupId: String?): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMissingStudentsByGroup(
        availableStudents: List<String>,
        groupId: String
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUnrelatedTeachersByCourseOrGroupAsCurator() {
        TODO("Not yet implemented")
    }

    override suspend fun isStudent(userId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getStudentIdsOfCourseByCourseId(courseId: String): List<String> {
        TODO("Not yet implemented")
    }
}