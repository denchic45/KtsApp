package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.EventsOfDay
import com.denchic45.kts.data.model.room.DayWithEventsEntities
import org.mapstruct.Mapper

@Mapper(uses = [EventMapper::class])
abstract class DayMapper {

    abstract fun entityToDomain(entities: DayWithEventsEntities): EventsOfDay
}