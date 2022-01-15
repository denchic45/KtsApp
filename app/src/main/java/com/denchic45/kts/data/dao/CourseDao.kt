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

    @Query("DELETE FROM course WHERE course_id IN(SELECT course_id FROM group_course WHERE group_id=:groupId)")
    abstract fun clearByGroupId(groupId: String)

    @Query("SELECT * FROM course WHERE course_id =:id")
    abstract fun get(id: String): Flow<CourseWithSubjectWithTeacherAndGroups>

    @Query("SELECT * FROM course WHERE course_id =:id")
    abstract fun getSync(id: String): CourseWithSubjectWithTeacherAndGroups

    @Query("SELECT c.* FROM course c JOIN group_course gc ON gc.course_id =c.course_id WHERE gc.group_id =:groupId")
    abstract fun getCoursesByGroupId(groupId: String): LiveData<List<CourseWithSubjectWithTeacherAndGroups>>

    @Query("SELECT c.* FROM course c JOIN group_course gc ON gc.course_id =c.course_id WHERE gc.group_id =:groupId")
    abstract fun getCoursesByGroupIdSync(groupId: String): List<CourseWithSubjectWithTeacherAndGroups>

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
        return teacherIds.stream()
            .filter { teacherId: String -> !hasRelatedTeacherToGroup(teacherId, groupId) }
            .collect(Collectors.toList())
    }

    @Transaction
    open fun getNotRelatedSubjectIdsToGroup(
        subjectIds: List<String>,
        groupId: String
    ): List<String> {
        return subjectIds.stream()
            .filter { subjectId: String -> !hasRelatedSubjectToGroup(subjectId, groupId) }
            .collect(Collectors.toList())
    }

    @Query("DELETE FROM course WHERE course_id NOT IN(SELECT c.course_id FROM course c INNER JOIN group_course gc INNER JOIN `group` g  ON c.course_id = gc.course_id AND g.group_id = gc.group_id)")
    abstract fun deleteUnrelatedByGroup()

    @Query("DELETE FROM course WHERE teacher_id =:teacherId AND course_id NOT IN(:availableCourseIds)")
    abstract fun deleteMissingByTeacher(availableCourseIds: List<String>, teacherId: String)

    @Query("SELECT * FROM course WHERE teacher_id=:id")
    abstract fun getByTeacherId(id: String): Flow<List<CourseWithSubjectAndTeacher>>

    @Query("DELETE FROM course WHERE course_id =:courseId")
    abstract fun deleteById(courseId: String)
}