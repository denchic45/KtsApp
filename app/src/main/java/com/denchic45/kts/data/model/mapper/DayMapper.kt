package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.EventsOfDay
import com.denchic45.kts.data.model.firestore.DayDoc
import com.denchic45.kts.data.model.room.DayEntity
import com.denchic45.kts.data.model.room.DayWithEventsEntities
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(uses = [EventMapper::class])
abstract class DayMapper {

    @Mapping(source = "dayEntity", target = ".")
    @Mapping(source = "eventEntities", target = "_events")
    abstract fun entityToDomain(entities: DayWithEventsEntities): EventsOfDay

    abstract fun docToEntity(dayDoc: DayDoc): DayEntity

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "eventsOfDay.events", target = "events")
    abstract fun domainToDoc(eventsOfDay: EventsOfDay, groupId: String): DayDoc
}