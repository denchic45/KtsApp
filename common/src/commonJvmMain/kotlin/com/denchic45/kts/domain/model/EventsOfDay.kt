package com.denchic45.kts.domain.model

import com.denchic45.kts.util.swap
import com.denchic45.kts.util.toString
import java.time.LocalDate

data class EventsOfDay(
    val date: LocalDate,
    private val _events: List<Event> = mutableListOf(),
    val startsAtZero: Boolean = false
) {

    val size: Int = _events.size

    val events: List<Event> =
        removeRedundantEmptyEvents(_events.onEach { it.eventsOfDay = this })

    val dayOfWeek: Int
        get() = date.dayOfWeek.value

    fun add(event: Event): EventsOfDay = copy(_events = events.map(Event::copy) + event)

    fun add(event: Event, order: Int): EventsOfDay {
        return copy(
            _events = events.map(Event::copy).toMutableList()
                .apply { add(indexOfOrder(order), event) }
        )
    }

    fun update(editedEvent: Event, index: Int = orderOf(editedEvent)): EventsOfDay {
        return copy(_events = events.toMutableList().apply {
            set(indexOfOrder(index), editedEvent)
        })
    }

    private fun indexOfOrder(order: Int): Int {
        return order - offsetIndex
    }

    private val offsetIndex: Int
        get() = if (startsAtZero) 0 else 1

    fun swap(oldIndex: Int, newIndex: Int): EventsOfDay {
        return copy(_events = events.swap(oldIndex, newIndex).map(Event::copy))
    }

    private fun removeRedundantEmptyEvents(events: List<Event>): List<Event> {
        if (events.isEmpty()) return events
        val updatedEvents: MutableList<Event> = events.toMutableList()

        while (true) {
            if (updatedEvents.last().isEmpty) {
                updatedEvents.removeLast()
                if (updatedEvents.isEmpty())
                    break
            } else {
                break
            }
        }

        return updatedEvents
    }

    fun remove(event: Event): EventsOfDay {
        return copy(_events = events.map(Event::copy) - event)
    }

    fun isEmpty(): Boolean = _events.isEmpty()

    fun last(): Event = _events.last()

    fun orderOf(event: Event): Int {
        return events.indexOfFirst { event.id == it.id } + offsetIndex
    }

    val weekName: String
        get() = date.toString("EEE")

    companion object {
        fun createEmpty(date: LocalDate): EventsOfDay = EventsOfDay(date, mutableListOf())
    }
}