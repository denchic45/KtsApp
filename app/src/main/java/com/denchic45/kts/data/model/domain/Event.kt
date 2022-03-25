package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.room.EventEntity.TYPE
import com.denchic45.kts.utils.UUIDS
import java.time.LocalDate
import java.util.*

data class Event(
    override var id: String = UUIDS.createShort(),
    val group: CourseGroup,
    val date: LocalDate,
    private val _order: Int = 0,
    val timestamp: Date? = null,
    val room: String = "",
    val details: EventDetails = EmptyEventDetails()
) : DomainModel() {

    var eventsOfDay: EventsOfDay? = null

    val isAttached: Boolean
        get() = eventsOfDay != null

    val isEmpty: Boolean
        get() = details.type == TYPE.EMPTY

    override fun copy(): Event {
        return Event(id, group, date, _order, timestamp, room, details)
    }

    val order: Int
        get() = eventsOfDay?.orderOf(this)
            ?: throw IllegalStateException("Event isn't attached to the events of day")

    val type: TYPE = details.type

    companion object {

        fun createEmpty(
            id: String = UUIDS.createShort(),
            group: CourseGroup,
            order: Int,
            date: LocalDate,
            details: EventDetails = EmptyEventDetails()
        ): Event {
            return Event(id, group, date, order, details = details)
        }
    }
}