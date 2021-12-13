package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.room.EventEntity.TYPE
import java.util.*

data class Event(
    override var uuid: String = UUID.randomUUID().toString(),
    val group: Group,
    val date: Date,
    val order: Int = 0,
    val timestamp: Date? = null,
    val room: String? = "",
    val details: EventDetails = EmptyEventDetails()
) : DomainModel() {

    val isEmpty: Boolean
        get() = details.type == TYPE.EMPTY

    override fun copy(): Event {
        return Event(uuid, group, date, order, timestamp, room, details)
    }

    val type: TYPE
        get() = details.type

    companion object {
        @JvmStatic
        fun empty(uuid: String = UUID.randomUUID().toString(), group: Group, order: Int, date: Date, details: EventDetails = EmptyEventDetails()): Event {
            return Event(uuid, group, date, order, details = details)
        }
    }
}