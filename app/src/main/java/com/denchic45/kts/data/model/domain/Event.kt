package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.room.EventEntity.TYPE
import java.time.LocalDate
import java.util.*

data class Event(
    override var id: String = UUID.randomUUID().toString(),
    val group: CourseGroup,
    val date: LocalDate,
    val order: Int = 0,
    val timestamp: Date? = null,
    val room: String = "",
    val details: EventDetails = EmptyEventDetails()
) : DomainModel() {

    val isEmpty: Boolean
        get() = details.type == TYPE.EMPTY

    override fun copy(): Event {
        return Event(id, group, date, order, timestamp, room, details)
    }

    val type: TYPE
        get() = details.type

    companion object {
        @JvmStatic
        fun empty(id: String = UUID.randomUUID().toString(), group: CourseGroup, order: Int, date: LocalDate, details: EventDetails = EmptyEventDetails()): Event {
            return Event(id, group, date, order, details = details)
        }
    }
}