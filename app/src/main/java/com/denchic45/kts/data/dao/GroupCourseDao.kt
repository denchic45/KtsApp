package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.GroupCourseCrossRef

@Dao
abstract class GroupCourseDao : BaseDao<GroupCourseCrossRef>() {

    @Query("DELETE FROM group_course WHERE group_id =:groupId")
    abstract suspend fun deleteByGroup(groupId: String)

    @Query("DELETE FROM group_course WHERE course_id =:courseId")
    abstract suspend fun deleteByCourse(courseId: String)

//    @Query("DELETE FROM group_course WHERE group_id =:groupId AND course_id NOT IN(:availableCourseIds)")
//    abstract suspend fun deleteMissingByGroup(availableCourseIds: List<String>, groupId: String)
}