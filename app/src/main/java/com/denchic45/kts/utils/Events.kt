package com.denchic45.kts.utils

import com.denchic45.kts.data.model.domain.Event
import java.util.*

object Events {
    private const val ZERO_ORDER = 0
    private const val FIRST_ORDER = 1
    const val OFFSET_AFTER_REMOVE = 1
    fun removeRedundantEmptyEvents(events: MutableList<Event>) {
        if (events.isEmpty()) return
        val emptyEvents: MutableList<Event> = ArrayList()
        val firstEvent = events[0]
        if (firstEvent.order == 0 && firstEvent.isEmpty) {
            emptyEvents.add(firstEvent)
        }
        var i = events.size
        while (i-- > 0) {
            val event = events[i]
            if (event.isEmpty) {
                emptyEvents.add(event)
            } else break
        }
        events.removeAll(emptyEvents)
    }

    fun addMissingEmptyEvents(events: MutableList<Event>): List<Event> {
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
        events.clear()
        events.addAll(updatedList)
        return updatedList
    }

    fun swap(events: MutableList<Event>, shiftedEvent: Event, movedEvent: Event) {
        val indexOfMovedEvent = events.indexOf(movedEvent)
        val indexOfShiftedEvent = events.indexOf(shiftedEvent)
        val oldOrder = movedEvent.order
        val targetOrder = shiftedEvent.order
        events[indexOfMovedEvent] = events[indexOfMovedEvent].copy(order = targetOrder)
        events[indexOfShiftedEvent] = events[indexOfShiftedEvent].copy(order = oldOrder)
        Collections.swap(events, indexOfMovedEvent, indexOfShiftedEvent)
    }

    fun add(events: MutableList<Event>, event: Event) {
        val indexOfOrder = getIndex(events, event)
        addEmptyEventsIfNecessary(events, event)
        incrementOrdersIfNecessary(events, event)
        events.add(
            indexOfOrder,
            event
        )
    }

    fun update(events: MutableList<Event>, editedEvent: Event) {
        val oldEvent = findByUuid(events, editedEvent.id)
        events.remove(oldEvent)
        if (oldEvent.order > editedEvent.order) {
            incrementOrdersIfNecessary(events, editedEvent)
        } else if (oldEvent.order < editedEvent.order) {
            decrementOrdersIfNecessary(events, editedEvent, oldEvent)
        }
        addEmptyEventsIfNecessary(events, editedEvent)
        events.add(getIndex(events, editedEvent), editedEvent)
        removeRedundantEmptyEvents(events)
//        addMissingEmptyEvents(events)
    }

    private fun incrementOrdersIfNecessary(
        events: MutableList<Event>,
        editedEvent: Event
    ) {
        val eventWithConflictedOrder = findConflictEventByOrder(events, editedEvent)
        if (eventWithConflictedOrder != null) {
            if (!eventWithConflictedOrder.isEmpty)
                incrementOrders(eventWithConflictedOrder.order - 1, events)
            else {
                events.remove(eventWithConflictedOrder)
            }
        }
    }

    private fun decrementOrdersIfNecessary(
        events: MutableList<Event>,
        editedEvent: Event,
        oldEvent: Event
    ) {
        val eventWithConflictedOrder = findConflictEventByOrder(events, editedEvent)
        if (eventWithConflictedOrder != null) {
            if (!eventWithConflictedOrder.isEmpty)
                decrementOrders(events, getIndex(events, oldEvent), getIndex(events, eventWithConflictedOrder))
            else {
                events.remove(eventWithConflictedOrder)
            }
        } else {
            decrementOrders(events, getIndex(events, oldEvent))
        }
    }

    private fun addEmptyEventsIfNecessary(
        events: MutableList<Event>,
        editedEvent: Event
    ) {
        if (events.size < editedEvent.order - 1) {
            for (i in events.size until editedEvent.order - 1) {
                val updatedOrder = if (events.isNotEmpty()) events.last().order + 1 else 1
                events.add(
                    Event.empty(
                        group = editedEvent.group,
                        order = updatedOrder,
                        date = editedEvent.date
                    )
                )
            }
        }
    }

    private fun getIndex(events: List<Event>, event: Event): Int {
        return if (event.order > 0) event.order - 1 else 0 + if (events.first().order == 0) 1 else 0
    }

    private fun decrementOrders(
        events: MutableList<Event>,
        startIndex: Int,
        endIndex: Int = events.size
    ) {
        for (event in events.subList(startIndex, endIndex)) {
            events[events.indexOf(event)] = event.copy(order = event.order - 1)
        }
    }

    private fun incrementOrders(startIndex: Int, events: MutableList<Event>) {
        for (event in events.subList(startIndex, events.size)) {
            events[events.indexOf(event)] = event.copy(order = event.order + 1)
        }
    }

    fun remove(events: MutableList<Event>, event: Event) {
        val indexOfRemoved = events.indexOf(event)
        events.remove(event)
        decrementOrders(events, indexOfRemoved)
        removeRedundantEmptyEvents(events)
    }

    private fun findConflictEventByOrder(events: List<Event>, editedEvent: Event): Event? {
        return events.stream()
            .filter { event: Event -> editedEvent.order == event.order }
            .findFirst()
            .orElse(null)
    }

    private fun findByUuid(events: List<Event>, id: String): Event {
        return events.stream()
            .filter { event: Event -> event.id == id }
            .findFirst()
            .orElse(null)
    }
}