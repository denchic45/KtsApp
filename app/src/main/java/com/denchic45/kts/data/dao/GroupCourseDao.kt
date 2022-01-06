package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.dao.BaseDao
import com.denchic45.kts.data.model.room.GroupCourseCrossRef

@Dao
abstract class GroupCourseDao : BaseDao<GroupCourseCrossRef?>() {

    @Query("DELETE FROM group_course WHERE group_id =:groupUuid")
    abstract fun deleteByGroup(groupUuid: String)

    @Query("DELETE FROM group_course WHERE uuid_course =:courseUuid")
    abstract fun deleteByCourse(courseUuid: String)

    @Query("DELETE FROM group_course WHERE group_id =:groupUuid AND uuid_course NOT IN(:availableCourseUuids)")
    abstract fun deleteMissingByGroup(availableCourseUuids: List<String>, groupUuid: String)
}