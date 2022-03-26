package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@Dao
abstract class EventDao : BaseDao<EventEntity>() {
    @Query("SELECT * FROM event WHERE event_id=:id")
    abstract fun get(id: String): EventEntity?

    @Transaction
    fun observeEventsByDateAndGroupId(
        @TypeConverters(
            LocalDateConverter::class
        ) date: LocalDate, groupId: String
    ): Flow<DayWithEventsEntities> {
        return getDayByDateAndGroupId(date, groupId)
            .flatMapLatest { dayEntity ->
                observeEventsByDayId(dayEntity.id).map { events ->
                    DayWithEventsEntities(dayEntity, events)
                }
            }
    }

    @Query("SELECT * FROM day WHERE date=:date AND group_id =:groupId")
    abstract fun getDayByDateAndGroupId(date: LocalDate, groupId: String): Flow<DayEntity>

    @Query("SELECT * FROM event WHERE day_id=:dayId ORDER BY position")
    abstract fun observeEventsByDayId(dayId: String): Flow<List<EventWithSubjectAndTeachersEntities>>

    @Transaction
    @Query("SELECT * FROM event e JOIN teacher_event tl ON e.event_id == tl.event_id WHERE e.date=:date AND tl.user_id =:teacherId ORDER BY position")
    abstract fun observeEventsByDateAndTeacherId(
        @TypeConverters(
            LocalDateConverter::class
        ) date: LocalDate, teacherId: String
    ): Flow<List<EventWithSubjectAndTeachersEntities>>

    @Query("DELETE FROM day WHERE date BETWEEN :start AND :end AND group_id =:groupId")
    abstract suspend fun deleteByGroupAndDateRange(
        groupId: String,
        @TypeConverters(LocalDateConverter::class)
        start: LocalDate,
        @TypeConverters(LocalDateConverter::class)
        end: LocalDate
    )

    @Query("DELETE FROM day WHERE date =:date AND group_id =:groupId")
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