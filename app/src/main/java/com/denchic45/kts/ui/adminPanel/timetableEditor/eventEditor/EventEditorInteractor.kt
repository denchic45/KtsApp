package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor

import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.EventDetails
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventEditorInteractor @Inject constructor() {
    private var editedEvent = Channel<Resource<Pair<Event, String>>>()
    var oldEvent: MutableStateFlow<Event?> = MutableStateFlow(null)
    var isNew = false
        private set
    lateinit var validateEventDetails: (() -> Boolean)

    fun setEditedEvent(editedEvent: Event, isNew: Boolean) {
        this.oldEvent.value = editedEvent.copy()
        this.isNew = isNew
    }

    lateinit var getDetails: (() -> EventDetails)

    suspend fun receiveEvent(): Resource<Pair<Event, String>> {
        return editedEvent.receive()
    }

    fun observeOldEvent(): StateFlow<Event?> = oldEvent

    suspend fun postEvent(event: Resource.Success<Pair<Event, String>>) {
        editedEvent.send(event)
    }

    companion object {
        const val LESSON_CREATED = "LESSON_CREATED"
        const val LESSON_EDITED = "LESSON_EDITED"
        const val LESSON_REMOVED = "LESSON_REMOVED"
    }
}