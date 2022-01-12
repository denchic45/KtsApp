package com.denchic45.kts.ui.adminPanel.timtableEditor.eventEditor

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.EventDetails
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class EventEditorInteractor : Interactor {
    private var editedEvent = PublishSubject.create<Resource<Event>>()
    var oldEvent: MutableStateFlow<Event?> = MutableStateFlow(null)
    var isNew = false
        private set
    lateinit var validateEventDetails: (() -> Boolean)
    fun setEditedEvent(editedEvent: Event, isNew: Boolean) {
        this.oldEvent.value = editedEvent.copy()
        this.isNew = isNew
    }

    lateinit var getDetails: (() -> EventDetails)
    override fun removeListeners() = editedEvent.onComplete()

    fun observeEvent(): Observable<Resource<Event>> {
        if (editedEvent.hasComplete()) editedEvent = PublishSubject.create()
        return editedEvent
    }

    fun observeOldEvent(): StateFlow<Event?> = oldEvent

    fun postEvent(event: Resource<Event>) {
        editedEvent.onNext(event)
    }

    companion object {
        const val LESSON_CREATED = "LESSON_CREATED"
        const val LESSON_EDITED = "LESSON_EDITED"
        const val LESSON_REMOVED = "LESSON_REMOVED"
    }
}