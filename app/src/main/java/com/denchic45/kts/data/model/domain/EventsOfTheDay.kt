package com.denchic45.kts.data.model.domain

import java.util.*

data class EventsOfTheDay(
    val date: Date,
    val events: MutableList<Event> = mutableListOf()
) {
    fun add(event: Event) {
        events.add(event)
    }
}