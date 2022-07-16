package com.denchic45.kts.data.model.domain

import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.data.model.room.EventEntity.TYPE
import com.denchic45.kts.util.UUIDS
import java.time.LocalDate
import java.util.*

data class Event(
    override var id: String = UUIDS.createShort(),
    val groupHeader: GroupHeader,
    private val _order: Int = 0,
    val timestamp: Date? = null,
    val room: String = "",
    val details: EventDetails = EmptyEventDetails()
) : DomainModel {

    var eventsOfDay: EventsOfDay? = null

    val isAttached: Boolean
        get() = eventsOfDay != null

    val date: LocalDate
        get() = requireEventsOfDay().date

    val isEmpty: Boolean
        get() = details.type == TYPE.EMPTY

    override fun copy(): Event {
        return Event(id, groupHeader, _order, timestamp, room, details)
    }

    private fun requireEventsOfDay(): EventsOfDay {
        return eventsOfDay
            ?: throw IllegalStateException("Event isn't attached to the events of day")
    }

    val order: Int
        get() = requireEventsOfDay().orderOf(this)

    val type: TYPE = details.type

    companion object {

        fun createEmpty(
            id: String = UUIDS.createShort(),
            groupHeader: GroupHeader,
            order: Int,
            details: EventDetails = EmptyEventDetails()
        ): Event {
            return Event(id, groupHeader, order, details = details)
        }
    }
}