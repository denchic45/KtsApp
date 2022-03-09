package com.denchic45.kts.data.model.domain

import com.denchic45.kts.utils.toString
import java.time.LocalDate
import java.util.*

data class EventsOfDay(
    val date: LocalDate,
    private val _events: MutableList<Event> = mutableListOf()
) {

    init {
        addMissingEmptyEvents()
    }

    var events: List<Event> = _events
        get() = _events
        private set

    fun add(event: Event) {
        val indexOfOrder = getIndex(event)
        addEmptyEventsIfNecessary(event)
        incrementOrdersIfNecessary(event)
        _events.add(
            indexOfOrder,
            event
        )
    }

    fun addSimple(event: Event) {
        _events.add(event)
    }

    fun update(editedEvent: Event) {
        val oldEvent = findById(editedEvent.id)
        _events.remove(oldEvent)
        if (oldEvent.order > editedEvent.order) {
            incrementOrdersIfNecessary(editedEvent)
        } else if (oldEvent.order < editedEvent.order) {
            decrementOrdersIfNecessary(editedEvent, oldEvent)
        }
        addEmptyEventsIfNecessary(editedEvent)
        _events.add(getIndex(editedEvent), editedEvent)
        removeRedundantEmptyEvents()
//        addMissingEmptyEvents(events)
    }

    private fun decrementOrdersIfNecessary(
        editedEvent: Event,
        oldEvent: Event
    ) {
        val eventWithConflictedOrder = findConflictEventByOrder(events, editedEvent)
        if (eventWithConflictedOrder != null) {
            if (!eventWithConflictedOrder.isEmpty)
                decrementOrders(
                    getIndex(oldEvent),
                    getIndex(eventWithConflictedOrder)
                )
            else {
                _events.remove(eventWithConflictedOrder)
            }
        } else {
            decrementOrders(getIndex(oldEvent))
        }
    }

    private fun findById(id: String): Event {
        return _events.first { event: Event -> event.id == id }
    }

    private fun addEmptyEventsIfNecessary(
        editedEvent: Event
    ) {
        if (events.size < editedEvent.order - 1) {
            for (i in events.size until editedEvent.order - 1) {
                val updatedOrder = if (events.isNotEmpty()) events.last().order + 1 else 1
                _events.add(
                    Event.empty(
                        group = editedEvent.group,
                        order = updatedOrder,
                        date = editedEvent.date
                    )
                )
            }
        }
    }

    private fun incrementOrdersIfNecessary(
        editedEvent: Event
    ) {
        val eventWithConflictedOrder = findConflictEventByOrder(events, editedEvent)
        if (eventWithConflictedOrder != null) {
            if (!eventWithConflictedOrder.isEmpty)
                incrementOrders(eventWithConflictedOrder.order - 1)
            else {
                _events.remove(eventWithConflictedOrder)
            }
        }
    }

    fun swap(old:Int, new:Int) {
        swap(events[old], events[new])
    }

    fun swap(shiftedEvent: Event, movedEvent: Event) {
        val indexOfMovedEvent = _events.indexOf(movedEvent)
        val indexOfShiftedEvent = _events.indexOf(shiftedEvent)
        val oldOrder = movedEvent.order
        val targetOrder = shiftedEvent.order
        _events[indexOfMovedEvent] = _events[indexOfMovedEvent].copy(order = targetOrder)
        _events[indexOfShiftedEvent] = _events[indexOfShiftedEvent].copy(order = oldOrder)
        Collections.swap(_events, indexOfMovedEvent, indexOfShiftedEvent)
    }

    private fun getIndex(event: Event): Int {
        return if (event.order > 0) event.order - 1 else 0 + if (_events.first().order == 0) 1 else 0
    }

    private fun findConflictEventByOrder(events: List<Event>, editedEvent: Event): Event? {
        return events.stream()
            .filter { event: Event -> editedEvent.order == event.order }
            .findFirst()
            .orElse(null)
    }

    private fun incrementOrders(startIndex: Int) {
        for (event in events.subList(startIndex, events.size)) {
            _events[events.indexOf(event)] = event.copy(order = event.order + 1)
        }
    }

    fun removeRedundantEmptyEvents() {
        if (_events.isEmpty()) return
        val emptyEvents: MutableList<Event> = ArrayList()
        val firstEvent = events[0]
        if (firstEvent.order == 0 && firstEvent.isEmpty) {
            emptyEvents.add(firstEvent)
        }
        var i = events.size
        while (i-- > 0) {
            val event = _events[i]
            if (event.isEmpty) {
                emptyEvents.add(event)
            } else break
        }
        _events.removeAll(emptyEvents)
    }

    private fun addMissingEmptyEvents(): List<Event> {
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
        _events.clear()
        _events.addAll(updatedList)
        return updatedList
    }

    fun remove(event: Event) {
        val indexOfRemoved = events.indexOf(event)
        _events.remove(event)
        decrementOrders(indexOfRemoved)
        removeRedundantEmptyEvents()
    }

    private fun decrementOrders(
        startIndex: Int,
        endIndex: Int = events.size
    ) {
        for (event in events.subList(startIndex, endIndex)) {
            _events[events.indexOf(event)] = event.copy(order = event.order - 1)
        }
    }

    fun isEmpty(): Boolean = _events.isEmpty()
    fun last(): Event = _events.last()

    val weekName: String
        get() = date.toString("EEE")

    companion object {
        private const val ZERO_ORDER = 0
        private const val FIRST_ORDER = 1
        const val OFFSET_AFTER_REMOVE = 1
    }
}