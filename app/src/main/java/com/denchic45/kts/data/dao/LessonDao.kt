package com.denchic45.kts.data.dao

import androidx.room.Dao
import com.denchic45.kts.data.model.room.EventEntity
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.room.DateConverter
import androidx.room.Query
import androidx.room.Transaction
import com.denchic45.kts.data.model.room.EventTaskSubjectTeachersEntities
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
abstract class LessonDao : BaseDao<EventEntity?>() {
    @Query("SELECT * FROM event WHERE eventUuid=:uuid")
    abstract fun getByUuid(uuid: String?): EventEntity?
    @Transaction
    @Query("SELECT * FROM event WHERE date=:date AND groupUuid =:groupUuid ORDER BY `order`")
    abstract fun getLessonWithHomeWorkWithSubjectByDateAndGroupUuid(
        @TypeConverters(
            DateConverter::class
        ) date: Date, groupUuid: String
    ): Flow<List<EventTaskSubjectTeachersEntities>>

    @Transaction
    @Query("SELECT * FROM event l JOIN teacher_event tl ON l.eventUuid == tl.eventUuid WHERE date=:date AND tl.uuid_user =:teacherUuid ORDER BY `order`")
    abstract fun getLessonWithHomeWorkWithSubjectByDateAndTeacherUuid(
        @TypeConverters(
            DateConverter::class
        ) date: Date, teacherUuid: String
    ): Flow<List<EventTaskSubjectTeachersEntities>>

    @Query("DELETE FROM event WHERE date BETWEEN :start AND :end AND groupUuid =:groupUuid")
    abstract fun deleteByGroupAndDateRange(
        groupUuid: String, @TypeConverters(
            DateConverter::class
        ) start: Date?, @TypeConverters(DateConverter::class) end: Date?
    )

    @Query("DELETE FROM event WHERE date =:date AND groupUuid =:groupUuid")
    abstract fun deleteByDateAndGroup(
        @TypeConverters(
            DateConverter::class
        ) date: Date?, groupUuid: String?
    )

    @Transaction
    open suspend fun replaceByDateAndGroup(
        lessonEntities: List<EventEntity?>,
        date: Date?,
        groupUuid: String?
    ) {
        deleteByDateAndGroup(date, groupUuid)
        insert(lessonEntities)
    }
}