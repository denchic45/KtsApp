package com.denchic45.kts.data.dao

import androidx.room.Dao
import com.denchic45.kts.data.dao.BaseDao
import com.denchic45.kts.data.model.room.UserEntity
import androidx.lifecycle.LiveData
import androidx.room.Query
import androidx.room.Transaction
import io.reactivex.rxjava3.core.Observable

@Dao
abstract class UserDao : BaseDao<UserEntity>() {

    @Query("SELECT * FROM user WHERE uuid_user_group =:groupUuid ORDER BY surname")
    abstract fun getByGroupUuid(groupUuid: String?): LiveData<List<UserEntity>>

    @Query("SELECT * FROM user WHERE uuid_user_group =:groupUuid ORDER BY surname")
    abstract fun getByGroupUuidSync(groupUuid: String?): List<UserEntity>

    @Query("SELECT EXISTS(SELECT * FROM user WHERE uuid_user = :uuid)")
    abstract fun isExist(uuid: String?): Boolean

    @Query("DELETE FROM user WHERE role = 'TEACHER'")
    abstract fun clearTeachers()

   @Query("SELECT * FROM user WHERE role IN('STUDENT','DEPUTY_HEADMAN','HEADMAN') AND uuid_user_group=:groupUuid")
    abstract fun getStudentsOfGroupByGroupUuid(groupUuid: String?): LiveData<List<UserEntity>>

    @Query("SELECT * FROM user where uuid_user =:uuid")
    abstract fun getByUuid(uuid: String): LiveData<UserEntity>

    @Query("SELECT * FROM user where uuid_user =:uuid")
    abstract fun getByUuidSync(uuid: String?): UserEntity?

    @Query("DELETE FROM user WHERE uuid_user IN(SELECT u.uuid_user FROM user u JOIN course c JOIN group_course gc ON c.uuid_teacher == u.uuid_user AND c.uuid_course == gc.uuid_course WHERE gc.uuid_group =:groupUuid AND u.uuid_user NOT IN (:availableUsers) )")
    abstract fun deleteMissingTeachersByGroup(availableUsers: List<String>, groupUuid: String)

    @Query("DELETE FROM user WHERE uuid_user=:uuid")
    abstract fun deleteByUuid(uuid: String?)

    @get:Query("SELECT * FROM user WHERE role IN ('TEACHER','HEAD_TEACHER')")
    abstract val allTeachers: LiveData<List<UserEntity>>

    @Transaction
    open fun updateCuratorByGroupUuid(groupUuid: String, teacherUuid: String) {
        val currentCurator = getCuratorSync(groupUuid)
        updateGroupUuid(currentCurator.uuid, null)
        updateGroupUuid(teacherUuid, groupUuid)
    }

    @Query("UPDATE user SET uuid_user_group =:groupUuid WHERE uuid_user =:userUuid")
    abstract fun updateGroupUuid(userUuid: String, groupUuid: String?)

    @Query("SELECT * FROM user u INNER JOIN `group` g ON u.uuid_user = g.uuid_curator WHERE g.uuid_group=:groupUuid")
    abstract fun getCurator(groupUuid: String): LiveData<UserEntity>

    @Query("SELECT * FROM user u INNER JOIN `group` g ON u.uuid_user = g.uuid_curator WHERE g.uuid_group=:groupUuid")
    abstract fun getCuratorSync(groupUuid: String): UserEntity

    @Query("SELECT uuid_user_group FROM user where uuid_user_group =:groupUuid")
    abstract fun getGroupUuidOfUser(groupUuid: String): String

    @Query("SELECT * FROM user where uuid_user =:uuid")
    abstract fun getByUuidRx(uuid: String): Observable<UserEntity>

    @Query("SELECT EXISTS(SELECT * FROM user where uuid_user =:uuid AND uuid_user_group =:groupUuid)")
    abstract fun isExistByUuidAndGroupUuid(uuid: String, groupUuid: String?): Boolean

    @Query("DELETE FROM user WHERE uuid_user_group =:groupUuid AND uuid_user NOT IN(:availableStudents) ")
    abstract fun deleteMissingStudentsByGroup(availableStudents: List<String>, groupUuid: String)

    @Query("SELECT * FROM user WHERE role IN('TEACHER','HEAD_TEACHER') AND uuid_user NOT IN(SELECT u.uuid_user FROM user u INNER JOIN course c INNER JOIN `group` g ON c.uuid_teacher == u.uuid_user OR g.uuid_curator = u.uuid_user)")
    abstract fun findUnrelatedTeachersByCourseOrGroupAsCurator():List<UserEntity>

    @Query("DELETE FROM user WHERE role IN('TEACHER','HEAD_TEACHER') AND uuid_user NOT IN(SELECT u.uuid_user FROM user u LEFT JOIN course c LEFT JOIN `group` g ON c.uuid_teacher == u.uuid_user OR g.uuid_curator = u.uuid_user)")
    abstract fun deleteUnrelatedTeachersByCourseOrGroupAsCurator()
}