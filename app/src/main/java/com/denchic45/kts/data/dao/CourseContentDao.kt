package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.room.CourseContentEntity
import com.denchic45.kts.data.model.room.TimestampConverter
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
abstract class CourseContentDao : BaseDao<CourseContentEntity>() {

    @Query("SELECT * FROM course_content WHERE course_id =:courseId")
    abstract fun getByCourseId(courseId: String): Flow<List<CourseContentEntity>>

    @Query("SELECT * FROM course_content WHERE content_id=:id")
    abstract fun get(id: String): Flow<CourseContentEntity?>

    @Query("SELECT * FROM course_content WHERE content_id=:id")
    abstract suspend fun getSync(id: String): CourseContentEntity

    @Query("SELECT cc.attachments FROM course_content cc WHERE content_id=:id")
    abstract fun getAttachmentsById(id: String): Flow<String>

    @Query("SELECT cc.course_id FROM course_content cc WHERE content_id=:taskId")
    abstract suspend fun getCourseIdByTaskId(taskId: String): String

    @Query("SELECT course_id FROM course_content WHERE content_id=:taskId")
    abstract suspend fun getCourseId(taskId: String): String

    @Query(
        """
            SELECT MAX(cc.timestamp) FROM course_content cc 
            INNER JOIN group_course gc ON gc.course_id = cc.course_id
            WHERE cc.weekDate IN(:currentWeek, :nextWeek) AND gc.group_id=:groupId
            """
    )
    @TypeConverters(TimestampConverter::class)
    abstract fun getByMaxTimestampAndGroupIdAndCurrentWeekAndNextWeek(
        groupId: String,
        currentWeek: String,
        nextWeek: String
    ): Date

    @Query(
        """
            SELECT cc.* FROM course_content cc 
            INNER JOIN group_course gc ON gc.course_id = cc.course_id
            WHERE cc.weekDate IN(:currentWeek, :nextWeek) AND gc.group_id=:groupId
            """
    )
    abstract fun getByGroupIdAndCurrentWeekAndNextWeek(
        groupId: String,
        currentWeek: String,
        nextWeek: String
    ): Flow<List<CourseContentEntity>>

    @Query(
        """
            SELECT cc.* FROM course_content cc 
            INNER JOIN group_course gc ON gc.course_id = cc.course_id
            WHERE cc.completion_date >:startDate AND gc.group_id=:groupId
            """
    )
    abstract fun getByGroupIdAndGreaterCompletionDate(
        groupId: String,
        @TypeConverters(TimestampConverter::class) startDate: Date = Date()
    ): Flow<List<CourseContentEntity>>
}