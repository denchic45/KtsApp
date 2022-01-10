package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import com.denchic45.kts.data.model.room.CourseContentEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CourseContentDao : BaseDao<CourseContentEntity>() {

    @Query("SELECT * FROM course_content WHERE courseId =:courseId")
    abstract fun getByCourseUuid(courseId: String): Flow<List<CourseContentEntity>>

    @Query("SELECT * FROM course_content WHERE content_id=:id")
    abstract fun get(id: String): Flow<CourseContentEntity>

    @Query("SELECT cc.attachments FROM course_content cc WHERE content_id=:id")
    abstract fun getAttachmentsById(id: String): Flow<String>
}