package com.denchic45.kts.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.denchic45.kts.data.model.room.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao : BaseDao<UserEntity>() {

    @Query("SELECT * FROM user WHERE user_group_id =:groupId ORDER BY surname")
    abstract fun observeByGroupId(groupId: String): Flow<List<UserEntity>>

    @Query("SELECT * FROM user WHERE user_group_id =:groupId ORDER BY surname")
    abstract suspend fun getByGroupId(groupId: String): List<UserEntity>

    @Query("SELECT EXISTS(SELECT * FROM user WHERE user_id = :id)")
    abstract suspend fun isExist(id: String): Boolean

    @Query("DELETE FROM user WHERE role = 'TEACHER'")
    abstract suspend fun clearTeachers()

    @Query("SELECT * FROM user WHERE role IN('STUDENT','DEPUTY_HEADMAN','HEADMAN') AND user_group_id=:groupId")
    abstract fun getStudentsOfGroupByGroupId(groupId: String): LiveData<List<UserEntity>>

    @Query("SELECT * FROM user where user_id =:id")
    abstract suspend fun get(id: String): UserEntity?

    @Query("DELETE FROM user WHERE user_id IN(SELECT u.user_id FROM user u JOIN course c JOIN group_course gc ON c.teacher_id == u.user_id AND c.course_id == gc.course_id WHERE gc.group_id =:groupId AND u.user_id NOT IN (:availableUsers) )")
    abstract suspend fun deleteMissingTeachersByGroup(availableUsers: List<String>, groupId: String)

    @Query("DELETE FROM user WHERE user_id=:id")
    abstract suspend fun deleteById(id: String)

    @Transaction
    open suspend fun updateCuratorByGroupId(groupId: String, teacherId: String) {
        val currentCurator = getCurator(groupId)
        updateGroupId(currentCurator.id, null)
        updateGroupId(teacherId, groupId)
    }

    @Query("UPDATE user SET user_group_id =:groupId WHERE user_id =:userId")
    abstract suspend fun updateGroupId(userId: String, groupId: String?)

    @Query("SELECT * FROM user u INNER JOIN `group` g ON u.user_id = g.curator_id WHERE g.group_id=:groupId")
    abstract fun observeCurator(groupId: String): Flow<UserEntity>

    @Query("SELECT * FROM user u INNER JOIN `group` g ON u.user_id = g.curator_id WHERE g.group_id=:groupId")
    abstract suspend fun getCurator(groupId: String): UserEntity

    @Query("SELECT user_group_id FROM user where user_group_id =:groupId")
    abstract suspend fun getUserGroupId(groupId: String): String

    @Query("SELECT * FROM user where user_id =:id")
    abstract fun observe(id: String): Flow<UserEntity?>

    @Query("SELECT EXISTS(SELECT * FROM user where user_id =:id AND user_group_id =:groupId)")
    abstract suspend fun isExistByIdAndGroupId(id: String, groupId: String?): Boolean

    @Query("DELETE FROM user WHERE user_group_id =:groupId AND user_id NOT IN(:availableStudents) ")
    abstract suspend fun deleteMissingStudentsByGroup(availableStudents: List<String>, groupId: String)

    @Query("SELECT * FROM user WHERE role IN('TEACHER','HEAD_TEACHER') AND user_id NOT IN(SELECT u.user_id FROM user u INNER JOIN course c INNER JOIN `group` g ON c.teacher_id == u.user_id OR g.curator_id = u.user_id)")
    abstract suspend fun findUnrelatedTeachersByCourseOrGroupAsCurator(): List<UserEntity>

    @Query("DELETE FROM user WHERE role IN('TEACHER','HEAD_TEACHER') AND user_id NOT IN(SELECT u.user_id FROM user u LEFT JOIN course c LEFT JOIN `group` g ON c.teacher_id == u.user_id OR g.curator_id = u.user_id)")
    abstract suspend fun deleteUnrelatedTeachersByCourseOrGroupAsCurator()

    @Query("SELECT EXISTS(SELECT * FROM user WHERE user_id=:userId AND role IN('STUDENT','DEPUTY_HEADMAN','HEADMAN'))")
    abstract suspend fun isStudent(userId: String): Boolean

    @Query("SELECT u.user_id FROM user u JOIN group_course gc ON u.user_group_id = gc.group_id WHERE gc.course_id=:courseId")
    abstract suspend fun getStudentIdsOfCourseByCourseId(courseId: String): List<String>
}