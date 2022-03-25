package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor

import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.EventDetails
import com.denchic45.kts.data.model.domain.EventsOfDay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventEditorInteractor @Inject constructor() {
    private var editedEventOfDay = Channel<EventsOfDay>()
    var oldEventsOfDay: MutableStateFlow<EventsOfDay?> = MutableStateFlow(null)
    var oldEvent: MutableStateFlow<Event?> = MutableStateFlow(null)
//    var isNew = false
//        private set
    lateinit var validateEventDetails: (() -> Boolean)

    fun setEditedEvent(eventsOfDay: EventsOfDay, event: Event) {
        this.oldEventsOfDay.value = eventsOfDay
        this.oldEvent.value = event
//        this.isNew = isNew
    }

    lateinit var getDetails: (() -> EventDetails)

    val isNew: Boolean
    get() = !oldEvent.value!!.isAttached

    suspend fun receiveEvent(): EventsOfDay {
        return editedEventOfDay.receive()
    }

    fun observeOldEventOfDay(): StateFlow<EventsOfDay?> = oldEventsOfDay

    fun observeOldEvent(): StateFlow<Event?> = oldEvent

    suspend fun postEvent(event: (EventsOfDay) -> EventsOfDay) {
        editedEventOfDay.send(event(oldEventsOfDay.value!!))
    }

    companion object {
        const val LESSON_CREATED = "LESSON_CREATED"
        const val LESSON_EDITED = "LESSON_EDITED"
        const val LESSON_REMOVED = "LESSON_REMOVED"
    }
}