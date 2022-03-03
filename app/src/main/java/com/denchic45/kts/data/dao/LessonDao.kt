package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.room.DateConverter
import com.denchic45.kts.data.model.room.EventEntity
import com.denchic45.kts.data.model.room.EventWithSubjectAndTeachersEntities
import com.denchic45.kts.data.model.room.LocalDateConverter
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.util.*

@Dao
abstract class LessonDao : BaseDao<EventEntity>() {
    @Query("SELECT * FROM event WHERE event_id=:id")
    abstract fun get(id: String): EventEntity?

    @Transaction
    @Query("SELECT * FROM event WHERE date=:date AND group_id =:groupId ORDER BY `order`")
    abstract fun getLessonWithHomeWorkWithSubjectByDateAndGroupId(
        @TypeConverters(
            LocalDateConverter::class
        ) date: LocalDate, groupId: String
    ): Flow<List<EventWithSubjectAndTeachersEntities>>

    @Transaction
    @Query("SELECT * FROM event l JOIN teacher_event tl ON l.event_id == tl.event_id WHERE date=:date AND tl.user_id =:teacherId ORDER BY `order`")
    abstract fun getLessonWithHomeWorkWithSubjectByDateAndTeacherId(
        @TypeConverters(
            LocalDateConverter::class
        ) date: LocalDate, teacherId: String
    ): Flow<List<EventWithSubjectAndTeachersEntities>>

    @Query("DELETE FROM event WHERE date BETWEEN :start AND :end AND group_id =:groupId")
    abstract fun deleteByGroupAndDateRange(
        groupId: String,
        @TypeConverters(LocalDateConverter::class)
        start: LocalDate,
        @TypeConverters(LocalDateConverter::class)
        end: LocalDate
    )

    @Query("DELETE FROM event WHERE date =:date AND group_id =:groupId")
    abstract fun deleteByDateAndGroup(
        @TypeConverters(
            LocalDateConverter::class
        ) date: LocalDate, groupId: String
    )

    @Transaction
    open suspend fun replaceByDateAndGroup(
        lessonEntities: List<EventEntity>,
        date: LocalDate,
        groupId: String
    ) {
        deleteByDateAndGroup(date, groupId)
        insert(lessonEntities)
    }
}