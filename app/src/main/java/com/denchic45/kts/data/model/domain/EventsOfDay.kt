package com.denchic45.kts.data.model.domain

import com.denchic45.kts.utils.toString
import java.time.LocalDate
import java.util.*

data class EventsOfDay(
    val date: LocalDate,
    private val startedEvents: List<Event> = mutableListOf()
) {

    private val _events: List<Event> =
        removeRedundantEmptyEvents(addMissingEmptyEvents(startedEvents))

    val events: List<Event>
        get() = _events

    fun add(event: Event): EventsOfDay {
        var updatedEvents: List<Event> = events
        val indexOfOrder = getIndex(updatedEvents, event)
        updatedEvents = addEmptyEventsIfNecessary(updatedEvents, event)
        updatedEvents = incrementOrdersIfNecessary(updatedEvents, event)
        updatedEvents = updatedEvents.toMutableList().apply {
            add(indexOfOrder, event)
        }
        return EventsOfDay(date, updatedEvents)
    }

    fun update(editedEvent: Event): EventsOfDay {
        var updatedEvents: MutableList<Event> = events.toMutableList()
        val oldEvent = findById(editedEvent.id)
        updatedEvents.remove(oldEvent)
        if (oldEvent.order > editedEvent.order) {
            updatedEvents = incrementOrdersIfNecessary(updatedEvents, editedEvent).toMutableList()
        } else if (oldEvent.order < editedEvent.order) {
            updatedEvents =
                decrementOrdersIfNecessary(updatedEvents, editedEvent, oldEvent).toMutableList()
        }
        addEmptyEventsIfNecessary(updatedEvents, editedEvent)
        updatedEvents.add(getIndex(updatedEvents, editedEvent), editedEvent)
        removeRedundantEmptyEvents(updatedEvents)
//        addMissingEmptyEvents(events)
        return EventsOfDay(date, updatedEvents)
    }

    private fun decrementOrdersIfNecessary(
        events: List<Event>,
        editedEvent: Event,
        oldEvent: Event
    ): List<Event> {
        var updatedEvents: MutableList<Event> = events.toMutableList()
        val eventWithConflictedOrder = findConflictEventByOrder(updatedEvents, editedEvent)
        if (eventWithConflictedOrder != null) {
            if (!eventWithConflictedOrder.isEmpty)
                decrementOrders(
                    updatedEvents,
                    getIndex(updatedEvents, oldEvent),
                    getIndex(updatedEvents, eventWithConflictedOrder)
                )
            else {
                updatedEvents.remove(eventWithConflictedOrder)
            }
        } else {
            updatedEvents =
                decrementOrders(updatedEvents, getIndex(updatedEvents, oldEvent)).toMutableList()
        }
        return updatedEvents
    }

    private fun findById(id: String): Event {
        return startedEvents.first { event: Event -> event.id == id }
    }

    private fun addEmptyEventsIfNecessary(
        events: List<Event>,
        editedEvent: Event
    ): List<Event> {
        val updatedEvents: MutableList<Event> = events.toMutableList()
        if (updatedEvents.size < editedEvent.order - 1) {
            for (i in updatedEvents.size until editedEvent.order - 1) {
                val updatedOrder =
                    if (updatedEvents.isNotEmpty()) updatedEvents.last().order + 1 else 1
                updatedEvents.add(
                    Event.empty(
                        group = editedEvent.group,
                        order = updatedOrder,
                        date = editedEvent.date
                    )
                )
            }
        }
        return updatedEvents
    }

    private fun incrementOrdersIfNecessary(
        events: List<Event>,
        editedEvent: Event
    ): List<Event> {
        val updatedEvents: MutableList<Event> = events.toMutableList()
        val eventWithConflictedOrder = findConflictEventByOrder(updatedEvents, editedEvent)
        if (eventWithConflictedOrder != null) {
            if (!eventWithConflictedOrder.isEmpty)
                incrementOrders(updatedEvents, eventWithConflictedOrder.order - 1)
            else {
                updatedEvents.remove(eventWithConflictedOrder)
            }
        }
        return updatedEvents
    }

    fun swap(old: Int, new: Int): EventsOfDay {
        val updatedEvents: MutableList<Event> = events.toMutableList()
        val oldOrder = updatedEvents[new].order
        val targetOrder = updatedEvents[old].order
        updatedEvents[new] = updatedEvents[new].copy(order = targetOrder)
        updatedEvents[old] = updatedEvents[old].copy(order = oldOrder)
        Collections.swap(updatedEvents, new, old)
        return EventsOfDay(date, updatedEvents)
    }

    fun swap(events: MutableList<Event>, shiftedEvent: Event, movedEvent: Event) {
        val indexOfMovedEvent = startedEvents.indexOf(movedEvent)
        val indexOfShiftedEvent = startedEvents.indexOf(shiftedEvent)
        val oldOrder = movedEvent.order
        val targetOrder = shiftedEvent.order
        events[indexOfMovedEvent] = startedEvents[indexOfMovedEvent].copy(order = targetOrder)
        events[indexOfShiftedEvent] = startedEvents[indexOfShiftedEvent].copy(order = oldOrder)
        Collections.swap(startedEvents, indexOfMovedEvent, indexOfShiftedEvent)
    }

    private fun getIndex(events: List<Event>, event: Event): Int {
        return if (event.order > 0) event.order - 1 else 0 + if (events.first().order == 0) 1 else 0
    }

    private fun findConflictEventByOrder(events: List<Event>, editedEvent: Event): Event? {
        return events.stream()
            .filter { event: Event -> editedEvent.order == event.order }
            .findFirst()
            .orElse(null)
    }

    private fun incrementOrders(events: List<Event>, startIndex: Int): List<Event> {
        val updatedEvents: MutableList<Event> = events.toMutableList()
        for (event in updatedEvents.subList(startIndex, this.events.size)) {
            updatedEvents[this.events.indexOf(event)] = event.copy(order = event.order + 1)
        }
        return updatedEvents
    }

    private fun removeRedundantEmptyEvents(events: List<Event>): List<Event> {
        val updatedEvents: MutableList<Event> = events.toMutableList()
        if (updatedEvents.isEmpty()) return events
        val emptyEvents: MutableList<Event> = ArrayList()
        val firstEvent = updatedEvents[0]
        if (firstEvent.order == 0 && firstEvent.isEmpty) {
            emptyEvents.add(firstEvent)
        }
        var i = updatedEvents.size
        while (i-- > 0) {
            val event = updatedEvents[i]
            if (updatedEvents.isEmpty()) {
                updatedEvents.add(event)
            } else break
        }
        updatedEvents.removeAll(emptyEvents)
        return updatedEvents
    }

    private fun addMissingEmptyEvents(events: List<Event>): List<Event> {
        if (events.isEmpty()) return events
        val updatedList: MutableList<Event> = ArrayList()
        var offset = if (events[0].order == ZERO_ORDER) ZERO_ORDER else FIRST_ORDER
        for (i in events.indices) {
            val event = events[i]
            val order = event.order
            if (order > i + offset) {
                for (j in i + offset until order) {
                    updatedList.add(Event.empty(group = event.group, order = j, date = event.date))
                    offset++
                }
            }
            updatedList.add(event)
        }
//        _events.clear()
//        _events.addAll(updatedList)
        return updatedList
    }

    fun remove(event: Event): EventsOfDay {
        var updatedEvents: MutableList<Event> = events.toMutableList()
        val indexOfRemoved = updatedEvents.indexOf(event)
        updatedEvents.remove(event)
        updatedEvents = decrementOrders(updatedEvents, indexOfRemoved).toMutableList()
        updatedEvents = removeRedundantEmptyEvents(updatedEvents).toMutableList()
        return EventsOfDay(date, updatedEvents)
    }

    private fun decrementOrders(
        events: MutableList<Event>,
        startIndex: Int,
        endIndex: Int = events.size
    ): List<Event> {
        val updatedEvents: MutableList<Event> = events

        for (event in updatedEvents.subList(startIndex, endIndex)) {
            updatedEvents[updatedEvents.indexOf(event)] = event.copy(order = event.order - 1)
        }
        return updatedEvents
    }

    fun isEmpty(): Boolean = startedEvents.isEmpty()
    fun last(): Event = startedEvents.last()

    val weekName: String
        get() = date.toString("EEE")

    companion object {
        private const val ZERO_ORDER = 0
        private const val FIRST_ORDER = 1
        const val OFFSET_AFTER_REMOVE = 1

        fun createEmpty(date: LocalDate): EventsOfDay = EventsOfDay(date, mutableListOf())
    }
}