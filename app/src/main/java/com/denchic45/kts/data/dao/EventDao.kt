package com.denchic45.kts.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverters
import com.denchic45.kts.data.model.room.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate

@Dao
abstract class EventDao : BaseDao<EventEntity>() {

    @Query("SELECT * FROM event WHERE event_id=:id")
    abstract fun get(id: String): EventEntity?

    fun observeEventsByDateAndGroupId(
        date: LocalDate, groupId: String
    ): Flow<DayWithEventsEntities> {
        return getDayByDateAndGroupId(date, groupId)
            .flatMapLatest { dayEntity ->
                dayEntity?.let {
                    observeEventsByDayId(dayEntity.id).map { events ->
                        DayWithEventsEntities(dayEntity, events)
                    }
                } ?: flowOf(DayWithEventsEntities(DayEntity.createEmpty(date, groupId), emptyList()))
            }
    }

    @Query("SELECT * FROM day WHERE date=:date AND group_id =:groupId")
    abstract fun getDayByDateAndGroupId(
        @TypeConverters(LocalDateConverter::class)
        date: LocalDate,
        groupId: String
    ): Flow<DayEntity?>

    @Query("SELECT * FROM event WHERE day_id=:dayId ORDER BY position")
    abstract fun observeEventsByDayId(dayId: String): Flow<List<EventWithSubjectAndTeachersEntities>>

    @Transaction
    @Query("SELECT * FROM event e JOIN teacher_event te ON e.event_id == te.event_id JOIN day d ON d.day_id = e.day_id WHERE d.date=:date AND te.user_id =:teacherId ORDER BY position")
    abstract fun observeEventsByDateAndTeacherId(
        @TypeConverters(LocalDateConverter::class)
        date: LocalDate,
        teacherId: String
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
        @TypeConverters(LocalDateConverter::class)
        date: LocalDate,
        groupId: String
    )
}