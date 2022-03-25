package com.denchic45.kts.data.model.domain

import com.denchic45.kts.utils.swap
import com.denchic45.kts.utils.toString
import java.time.LocalDate

data class EventsOfDay(
    val date: LocalDate,
    private val startedEvents: List<Event> = mutableListOf(),
    private val startsOfZero: Boolean = false
) {

    private val _events: List<Event> = removeRedundantEmptyEvents(startedEvents.onEach { it.eventsOfDay = this })

    val events: List<Event>
        get() = _events

    val dayOfWeek: Int
        get() = date.dayOfWeek.value

    fun add(event: Event): EventsOfDay = copy(startedEvents = events + event)

    fun add(event: Event, order: Int): EventsOfDay {
        return copy(
            startedEvents = events.toMutableList()
                .apply { add(indexOfOrder(order), event) })
    }

    fun update(editedEvent: Event, index: Int = orderOf(editedEvent)): EventsOfDay {
        return copy(startedEvents = events.toMutableList().apply {
            set(indexOfOrder(index), editedEvent)
        })
    }

    private fun indexOfOrder(order: Int): Int {
        return order - offsetIndex
    }

    private val offsetIndex: Int
        get() = if (startsOfZero) 1 else 0

//    private fun decrementOrdersIfNecessary(
//        events: List<Event>,
//        editedEvent: Event,
//        oldEvent: Event
//    ): List<Event> {
//        var updatedEvents: MutableList<Event> = events.toMutableList()
//        val eventWithConflictedOrder = findConflictEventByOrder(updatedEvents, editedEvent)
//        if (eventWithConflictedOrder != null) {
//            if (!eventWithConflictedOrder.isEmpty)
//                decrementOrders(
//                    updatedEvents,
//                    getIndex(updatedEvents, oldEvent),
//                    getIndex(updatedEvents, eventWithConflictedOrder)
//                )
//            else {
//                updatedEvents.remove(eventWithConflictedOrder)
//            }
//        } else {
//            updatedEvents =
//                decrementOrders(updatedEvents, getIndex(updatedEvents, oldEvent)).toMutableList()
//        }
//        return updatedEvents
//    }
//
//    private fun findById(id: String): Event {
//        return startedEvents.first { event: Event -> event.id == id }
//    }
//
//    private fun addEmptyEventsIfNecessary(
//        events: List<Event>,
//        editedEvent: Event
//    ): List<Event> {
//        val updatedEvents: MutableList<Event> = events.toMutableList()
//        if (updatedEvents.size < editedEvent.order - 1) {
//            for (i in updatedEvents.size until editedEvent.order - 1) {
//                val updatedOrder =
//                    if (updatedEvents.isNotEmpty()) updatedEvents.last().order + 1 else 1
//                updatedEvents.add(
//                    Event.createEmpty(
//                        group = editedEvent.group,
//                        order = updatedOrder,
//                        date = editedEvent.date
//                    )
//                )
//            }
//        }
//        return updatedEvents
//    }
//
//    private fun incrementOrdersIfNecessary(
//        events: List<Event>,
//        editedEvent: Event
//    ): List<Event> {
//        val updatedEvents: MutableList<Event> = events.toMutableList()
//        val eventWithConflictedOrder = findConflictEventByOrder(updatedEvents, editedEvent)
//        if (eventWithConflictedOrder != null) {
//            if (!eventWithConflictedOrder.isEmpty)
//                incrementOrders(updatedEvents, eventWithConflictedOrder.order - 1)
//            else {
//                updatedEvents.remove(eventWithConflictedOrder)
//            }
//        }
//        return updatedEvents
//    }

    fun swap(oldIndex: Int, newIndex: Int): EventsOfDay {
        return copy(startedEvents = events.swap(oldIndex, newIndex))
    }

//    private fun getIndex(events: List<Event>, event: Event): Int {
//        return if (event.order > 0) event.order - 1 else 0 + if (events.first().order == 0) 1 else 0
//    }
//
//    private fun findConflictEventByOrder(events: List<Event>, editedEvent: Event): Event? {
//        return events.stream()
//            .filter { event: Event -> editedEvent.order == event.order }
//            .findFirst()
//            .orElse(null)
//    }
//
//    private fun incrementOrders(events: List<Event>, startIndex: Int): List<Event> {
//        val updatedEvents: MutableList<Event> = events.toMutableList()
//        for (event in updatedEvents.subList(startIndex, this.events.size)) {
//            updatedEvents[this.events.indexOf(event)] = event.copy(order = event.order + 1)
//        }
//        return updatedEvents
//    }

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

        if (updatedEvents.isEmpty()) return updatedEvents

//        while (true) {
//            if (updatedEvents.first().isEmpty) {
//                updatedEvents.removeFirst()
//                if (updatedEvents.isEmpty())
//                    break
//            } else {
//                break
//            }
//        }

//        if (updatedEvents.isEmpty()) return events
//        val emptyEvents: MutableList<Event> = ArrayList()
//        val firstEvent = updatedEvents[0]
//        if (firstEvent.order == 0 && firstEvent.isEmpty) {
//            emptyEvents.add(firstEvent)
//        }
//        var i = updatedEvents.size
//        while (i-- > 0) {
//            val event = updatedEvents[i]
//            if (updatedEvents.isEmpty()) {
//                updatedEvents.add(event)
//            } else break
//        }
//        updatedEvents.removeAll(emptyEvents)
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
                    updatedList.add(
                        Event.createEmpty(
                            group = event.group,
                            order = j,
                            date = event.date
                        )
                    )
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
        return copy(startedEvents = events - event)
    }

//    private fun decrementOrders(
//        events: MutableList<Event>,
//        startIndex: Int,
//        endIndex: Int = events.size
//    ): List<Event> {
//        val updatedEvents: MutableList<Event> = events
//
//        for (event in updatedEvents.subList(startIndex, endIndex)) {
//            updatedEvents[updatedEvents.indexOf(event)] = event.copy(order = event.order - 1)
//        }
//        return updatedEvents
//    }

    fun isEmpty(): Boolean = startedEvents.isEmpty()
    fun last(): Event = startedEvents.last()

    fun orderOf(event: Event): Int {
        return events.indexOfFirst { event.id == it.id } + offsetIndex
    }

    val weekName: String
        get() = date.toString("EEE")

    companion object {
        private const val ZERO_ORDER = 0
        private const val FIRST_ORDER = 1
        const val OFFSET_AFTER_REMOVE = 1

        fun createEmpty(date: LocalDate): EventsOfDay = EventsOfDay(date, mutableListOf())
    }
}