package com.denchic45.kts.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.denchic45.kts.data.model.room.CourseEntity
import com.denchic45.kts.data.model.room.CourseWithSubjectAndTeacher
import com.denchic45.kts.data.model.room.CourseWithSubjectWithTeacherAndGroups
import kotlinx.coroutines.flow.Flow
import java.util.stream.Collectors

@Dao
abstract class CourseDao : BaseDao<CourseEntity>() {

    @Query("DELETE FROM course WHERE uuid_course IN(SELECT uuid_course FROM group_course WHERE group_id=:group_id)")
    abstract fun clearByGroupUuid(group_id: String)

    @Query("SELECT * FROM course WHERE uuid_course =:uuid")
    abstract fun getByUuid(uuid: String): Flow<CourseWithSubjectWithTeacherAndGroups>

    @Query("SELECT * FROM course WHERE uuid_course =:uuid")
    abstract fun getSync(uuid: String): CourseWithSubjectWithTeacherAndGroups

    @Query("SELECT c.* FROM course c JOIN group_course gc ON gc.uuid_course =c.uuid_course WHERE gc.group_id =:groupUuid")
    abstract fun getCoursesByGroupUuid(groupUuid: String): LiveData<List<CourseWithSubjectWithTeacherAndGroups>>

    @Query("SELECT c.* FROM course c JOIN group_course gc ON gc.uuid_course =c.uuid_course WHERE gc.group_id =:groupUuid")
    abstract fun getCoursesByGroupUuidSync(groupUuid: String): List<CourseWithSubjectWithTeacherAndGroups>

    @Query("SELECT EXISTS(SELECT * FROM course c JOIN group_course gc ON gc.group_id =:groupUuid WHERE c.uuid_teacher= :teacherUuid)")
    abstract fun isGroupHasSuchTeacher(teacherUuid: String, groupUuid: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM course JOIN group_course gc ON gc.group_id =:groupUuid = :groupUuid AND uuid_teacher= :teacherUuid AND subject_id = :subjectUuid)")
    abstract fun isExistCourse(
        groupUuid: String,
        teacherUuid: String,
        subjectUuid: String
    ): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM course c JOIN group_course gc ON gc.group_id =:groupUuid WHERE uuid_teacher=:teacherUuid)")
    abstract fun hasRelatedTeacherToGroup(teacherUuid: String, groupUuid: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM course c JOIN group_course gc ON gc.group_id =:groupUuid WHERE subject_id=:subjectUuid)")
    abstract fun hasRelatedSubjectToGroup(subjectUuid: String, groupUuid: String): Boolean

    @Transaction
    open fun getNotRelatedTeacherIdsToGroup(
        teacherIds: List<String>,
        groupUuid: String
    ): List<String> {
        return teacherIds.stream()
            .filter { teacherUuid: String -> !hasRelatedTeacherToGroup(teacherUuid, groupUuid) }
            .collect(Collectors.toList())
    }

    @Transaction
    open fun getNotRelatedSubjectIdsToGroup(
        subjectIds: List<String>,
        groupUuid: String
    ): List<String> {
        return subjectIds.stream()
            .filter { subjectUuid: String -> !hasRelatedSubjectToGroup(subjectUuid, groupUuid) }
            .collect(Collectors.toList())
    }

//    @Query("DELETE FROM course WHERE uuid_course IN(SELECT c.uuid_course FROM course c JOIN group_course gc ON gc.uuid_course = c.uuid_course WHERE gc.group_id =:groupUuid AND gc.uuid_course NOT IN (:availableCourseUuids))")
//    abstract fun deleteMissingByGroup(availableCourseUuids: List<String>, groupUuid: String)

    @Query("DELETE FROM course WHERE uuid_course NOT IN(SELECT c.uuid_course FROM course c INNER JOIN group_course gc INNER JOIN `group` g  ON c.uuid_course = gc.uuid_course AND g.group_id = gc.group_id)")
    abstract fun deleteUnrelatedByGroup()

    @Query("DELETE FROM course WHERE uuid_teacher =:teacherUuid AND uuid_course NOT IN(:availableCourseUuids)")
    abstract fun deleteMissingByTeacher(availableCourseUuids: List<String>, teacherUuid: String)

    @Query("SELECT * FROM course WHERE uuid_teacher=:uuid")
    abstract fun getByTeacherUuid(uuid: String): Flow<List<CourseWithSubjectAndTeacher>>

    @Query("DELETE FROM course WHERE uuid_course =:courseUuid")
    abstract fun deleteByUuid(courseUuid: String)
}