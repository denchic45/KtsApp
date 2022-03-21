package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.denchic45.kts.data.model.room.CourseEntity
import com.denchic45.kts.data.model.room.CourseWithSubjectAndTeacherEntities
import com.denchic45.kts.data.model.room.CourseWithSubjectWithTeacherAndGroupsEntities
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseDao : BaseDao<CourseEntity>() {

    @Query("DELETE FROM course WHERE course_id IN(SELECT course_id FROM group_course WHERE group_id=:groupId)")
    abstract fun clearByGroupId(groupId: String)

    @Query("SELECT * FROM course WHERE course_id =:id")
    abstract fun observe(id: String): Flow<CourseWithSubjectWithTeacherAndGroupsEntities?>

    @Query("SELECT * FROM course WHERE course_id =:id")
    abstract suspend fun get(id: String): CourseWithSubjectWithTeacherAndGroupsEntities

    @Query("SELECT c.* FROM course c JOIN group_course gc ON gc.course_id =c.course_id WHERE gc.group_id =:groupId")
    abstract fun observeCoursesByGroupId(groupId: String): Flow<List<CourseWithSubjectAndTeacherEntities>>

    @Query("SELECT gc.course_id FROM group_course gc WHERE gc.group_id =:groupId")
    abstract fun getCourseIdsByGroupId(groupId: String): Flow<List<String>>

    @Query("SELECT c.* FROM course c JOIN group_course gc ON gc.course_id =c.course_id WHERE gc.group_id =:groupId")
    abstract suspend fun getCoursesByGroupId(groupId: String): List<CourseWithSubjectWithTeacherAndGroupsEntities>

    @Query("SELECT EXISTS(SELECT * FROM course c JOIN group_course gc ON gc.group_id =:groupId WHERE c.teacher_id= :teacherId)")
    abstract fun isGroupHasSuchTeacher(teacherId: String, groupId: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM course JOIN group_course gc ON gc.group_id =:groupId = :groupId AND teacher_id= :teacherId AND subject_id = :subjectId)")
    abstract fun isExistCourse(
        groupId: String,
        teacherId: String,
        subjectId: String
    ): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM course c JOIN group_course gc ON gc.group_id =:groupId WHERE teacher_id=:teacherId)")
    abstract fun hasRelatedTeacherToGroup(teacherId: String, groupId: String): Boolean

    @Query("SELECT EXISTS (SELECT 1 FROM course c JOIN group_course gc ON gc.group_id =:groupId WHERE subject_id=:subjectId)")
    abstract fun hasRelatedSubjectToGroup(subjectId: String, groupId: String): Boolean

    @Transaction
    open fun getNotRelatedTeacherIdsToGroup(
        teacherIds: List<String>,
        groupId: String
    ): List<String> {
        return teacherIds
            .filter { teacherId: String -> !hasRelatedTeacherToGroup(teacherId, groupId) }
    }

    @Transaction
    open fun getNotRelatedSubjectIdsToGroup(
        subjectIds: List<String>,
        groupId: String
    ): List<String> = subjectIds
        .filter { subjectId -> !hasRelatedSubjectToGroup(subjectId, groupId) }


    @Query("DELETE FROM course WHERE course_id NOT IN(SELECT c.course_id FROM course c INNER JOIN group_course gc INNER JOIN `group` g  ON c.course_id = gc.course_id AND g.group_id = gc.group_id)")
    abstract fun deleteUnrelatedByGroup()

    @Query("DELETE FROM course WHERE teacher_id =:teacherId AND course_id NOT IN(:availableCourseIds)")
    abstract suspend fun deleteMissingByTeacher(availableCourseIds: List<String>, teacherId: String)

    @Query("SELECT * FROM course WHERE teacher_id=:id")
    abstract fun getByTeacherId(id: String): Flow<List<CourseWithSubjectAndTeacherEntities>>

    @Query("DELETE FROM course WHERE course_id =:courseId")
    abstract fun deleteById(courseId: String)

    @Query("SELECT EXISTS (SELECT * FROM course  WHERE course_id=:courseId AND teacher_id=:teacherId)")
    abstract suspend fun isCourseTeacher(courseId: String, teacherId: String): Boolean

    @Query("SELECT course_id FROM course_content WHERE content_id=:taskId")
    abstract suspend fun getCourseIdByContentId(taskId: String): String
}