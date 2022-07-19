package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.simpleEventEditor

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.data.model.domain.SimpleEventDetails
import com.denchic45.kts.data.model.room.EventEntity
import com.denchic45.kts.data.model.ui.UiColor
import com.denchic45.kts.data.model.ui.UiImage
import com.denchic45.kts.domain.model.Event
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import javax.inject.Inject

class SimpleEventEditorViewModel @Inject constructor(
    private var interactor: EventEditorInteractor
) : BaseViewModel() {

    val showSelectedEvent = MutableLiveData<SimpleEventDetails>()

    val showEvents = SingleLiveData<List<ListItem>>()

    val showErrorField = MutableLiveData<Pair<Int, Boolean>>()

    private val events = listOf(
        SimpleEventDetails.dinner(),
        SimpleEventDetails.practice()
    )

    fun onEventClick() {
        showEvents.value = mapEventsToListItems()
    }

    private fun mapEventsToListItems(): List<ListItem> {
        return events
            .map { eventDetails: SimpleEventDetails ->
                ListItem(
                    title = eventDetails.name, color = UiColor.ColorName(eventDetails.color),
                    icon = UiImage.Url(eventDetails.iconUrl),
                )
            } + ListItem(title = "Создать", icon= UiImage.IdImage(R.drawable.ic_add))
    }

    fun onEventSelect(position: Int) {
        val selectedEvent = events[position]
        interactor.getDetails = { selectedEvent }
        showSelectedEvent.value = selectedEvent

    }

    init {

        with(interactor.oldEvent.value!!.details) {
            if (this.type == Event.TYPE.SIMPLE) {
                interactor.getDetails = { this as SimpleEventDetails }
                showSelectedEvent.value = this as SimpleEventDetails
            }
        }

        interactor.validateEventDetails = {
            UIValidator.of(
                Validation(Rule { showSelectedEvent.value != null })
                    .sendActionResult({
                        showErrorField.setValue(
                            R.id.rl_event to true
                        )
                    }) {
                        showErrorField.setValue(
                            R.id.rl_event to false
                        )
                    }
            ).runValidates()
        }
    }
}