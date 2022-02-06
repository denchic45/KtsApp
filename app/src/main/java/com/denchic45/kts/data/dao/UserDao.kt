package com.denchic45.kts.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.denchic45.kts.data.model.room.UserEntity
import io.reactivex.rxjava3.core.Observable

@Dao
abstract class UserDao : BaseDao<UserEntity>() {

    @Query("SELECT * FROM user WHERE user_group_id =:groupId ORDER BY surname")
    abstract fun getByGroupId(groupId: String): LiveData<List<UserEntity>>

    @Query("SELECT * FROM user WHERE user_group_id =:groupId ORDER BY surname")
    abstract fun getByGroupIdSync(groupId: String): List<UserEntity>

    @Query("SELECT EXISTS(SELECT * FROM user WHERE user_id = :id)")
    abstract fun isExist(id: String): Boolean

    @Query("DELETE FROM user WHERE role = 'TEACHER'")
    abstract fun clearTeachers()

    @Query("SELECT * FROM user WHERE role IN('STUDENT','DEPUTY_HEADMAN','HEADMAN') AND user_group_id=:groupId")
    abstract fun getStudentsOfGroupByGroupId(groupId: String): LiveData<List<UserEntity>>

    @Query("SELECT * FROM user where user_id =:id")
    abstract fun get(id: String): LiveData<UserEntity>?

    @Query("SELECT * FROM user where user_id =:id")
    abstract suspend fun getSync(id: String): UserEntity?

    @Query("DELETE FROM user WHERE user_id IN(SELECT u.user_id FROM user u JOIN course c JOIN group_course gc ON c.teacher_id == u.user_id AND c.course_id == gc.course_id WHERE gc.group_id =:groupId AND u.user_id NOT IN (:availableUsers) )")
    abstract fun deleteMissingTeachersByGroup(availableUsers: List<String>, groupId: String)

    @Query("DELETE FROM user WHERE user_id=:id")
    abstract fun deleteById(id: String)

    @get:Query("SELECT * FROM user WHERE role IN ('TEACHER','HEAD_TEACHER')")
    abstract val allTeachers: LiveData<List<UserEntity>>

    @Transaction
    open fun updateCuratorByGroupId(groupId: String, teacherId: String) {
        val currentCurator = getCuratorSync(groupId)
        updateGroupId(currentCurator.id, null)
        updateGroupId(teacherId, groupId)
    }

    @Query("UPDATE user SET user_group_id =:groupId WHERE user_id =:userId")
    abstract fun updateGroupId(userId: String, groupId: String?)

    @Query("SELECT * FROM user u INNER JOIN `group` g ON u.user_id = g.curator_id WHERE g.group_id=:groupId")
    abstract fun getCurator(groupId: String): LiveData<UserEntity>

    @Query("SELECT * FROM user u INNER JOIN `group` g ON u.user_id = g.curator_id WHERE g.group_id=:groupId")
    abstract fun getCuratorSync(groupId: String): UserEntity

    @Query("SELECT user_group_id FROM user where user_group_id =:groupId")
    abstract fun getUserGroupId(groupId: String): String

    @Query("SELECT * FROM user where user_id =:id")
    abstract fun getRx(id: String): Observable<UserEntity>

    @Query("SELECT EXISTS(SELECT * FROM user where user_id =:id AND user_group_id =:groupId)")
    abstract fun isExistByIdAndGroupId(id: String, groupId: String?): Boolean

    @Query("DELETE FROM user WHERE user_group_id =:groupId AND user_id NOT IN(:availableStudents) ")
    abstract fun deleteMissingStudentsByGroup(availableStudents: List<String>, groupId: String)

    @Query("SELECT * FROM user WHERE role IN('TEACHER','HEAD_TEACHER') AND user_id NOT IN(SELECT u.user_id FROM user u INNER JOIN course c INNER JOIN `group` g ON c.teacher_id == u.user_id OR g.curator_id = u.user_id)")
    abstract fun findUnrelatedTeachersByCourseOrGroupAsCurator(): List<UserEntity>

    @Query("DELETE FROM user WHERE role IN('TEACHER','HEAD_TEACHER') AND user_id NOT IN(SELECT u.user_id FROM user u LEFT JOIN course c LEFT JOIN `group` g ON c.teacher_id == u.user_id OR g.curator_id = u.user_id)")
    abstract fun deleteUnrelatedTeachersByCourseOrGroupAsCurator()

    @Query("SELECT EXISTS(SELECT * FROM user WHERE user_id=:userId AND role IN('STUDENT','DEPUTY_HEADMAN','HEADMAN'))")
    abstract fun isStudent(userId: String): Boolean
}