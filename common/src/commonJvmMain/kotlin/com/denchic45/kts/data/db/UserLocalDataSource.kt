package com.denchic45.kts.data.db

import com.denchic45.kts.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserLocalDataSource {

    suspend fun upsert(userEntity: UserEntity)

    suspend fun upsert(userEntities: List<UserEntity>)

    fun observeByGroupId(groupId: String): Flow<List<UserEntity>>

    suspend fun isExist(id: String): Boolean

    suspend fun clearTeachers()

    suspend fun get(id: String): UserEntity?

    suspend fun getAll(): List<UserEntity>

    suspend fun deleteMissingTeachersByGroup(availableUsers: List<String>, groupId: String)

    suspend fun deleteById(id: String)

    suspend fun updateCuratorByGroupId(groupId: String, teacherId: String) {
        val currentCurator = getCurator(groupId)
        updateGroupId(currentCurator.user_id, null)
        updateGroupId(teacherId, groupId)
    }

    suspend fun updateGroupId(userId: String, groupId: String?)

    fun observeCurator(groupId: String): Flow<UserEntity?>

    suspend fun getCurator(groupId: String): UserEntity

    suspend fun getGroupId(userId: String): String

    fun observe(id: String): Flow<UserEntity?>

    suspend fun isExistByIdAndGroupId(id: String, groupId: String?): Boolean


    suspend fun deleteMissingStudentsByGroup(
        availableStudents: List<String>,
        groupId: String
    )

    suspend fun deleteUnrelatedTeachersByCourseOrGroupAsCurator()

    suspend fun isStudent(userId: String): Boolean

    suspend fun getStudentIdsOfCourseByCourseId(courseId: String): List<String>

    //TODO Добавить недостающцю сущность

//  fun observeStudentsWithCuratorByGroupId(groupId: String): Flow<GroupWithCuratorAndStudentsEntity?>
}