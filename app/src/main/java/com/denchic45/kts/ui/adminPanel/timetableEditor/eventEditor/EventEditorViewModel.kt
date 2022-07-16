package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.EmptyEventDetails
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.Event.Companion.createEmpty
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.util.DatePatterns
import com.denchic45.kts.util.UUIDS
import com.denchic45.kts.util.toString
import kotlinx.coroutines.launch
import javax.inject.Inject

class EventEditorViewModel @Inject constructor(
    private val interactor: EventEditorInteractor
) : BaseViewModel() {

    val showErrorField = MutableLiveData<Pair<Int, Boolean>>()

    val showDetailEditor = SingleLiveData<Int>()

    val dateField = MutableLiveData<String>()

    val orderField = SingleLiveData("1")

    val roomField = SingleLiveData("")

    val showListOfEventTypes = SingleLiveData<Pair<Array<CharSequence>, Int>>()

    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<Event> = UIEditor(!interactor.oldEvent.value!!.isAttached) {
        interactor.oldEvent.value!!.copy(
            room = roomField.value!!,
            details = interactor.getDetails()
        )
    }
    private val eventTypeNames = arrayOf<CharSequence>("Урок", "Другое событие", "Окно")

    init {
        toolbarTitle = "Редактировать урок"
    }

    private fun fillFields() {
        viewModelScope.launch {
            interactor.observeOldEvent().collect { event ->
                event?.let {
                    roomField.value = event.room
                    dateField.value =
                        interactor.oldEventsOfDay.value!!.date.toString(DatePatterns.dd_MMMM)
                    orderField.value =
                        if (interactor.isNew) (interactor.oldEventsOfDay.value!!.size + 1).toString() else event.order.toString()
                }
            }
        }
    }

    private fun saveChanges() {
        viewModelScope.launch {
            interactor.postEvent {
                if (uiEditor.isNew) {
                    it.add(uiEditor.item)
                } else {
                    it.update(uiEditor.item)
                }
            }
            finish()
        }
    }

    fun onFabClick() {
        uiValidator.runValidates { saveChanges() }
    }

    fun onRoomType(room: String) {
        roomField.postValue(room)
    }

    fun onOrderType(order: Int) {
        orderField.postValue(order.toString())
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            android.R.id.home -> {
                viewModelScope.launch { finish() }
            }
            R.id.option_duplicate_event -> {
                viewModelScope.launch {
                    if (uiValidator.runValidates()) {
                        interactor.postEvent { eventsOfDay ->
                            if (interactor.isNew) {
                                eventsOfDay
                                    .add(uiEditor.item)
                                    .add(uiEditor.item)
                            } else {
                                eventsOfDay.update(
                                    uiEditor.item.copy(),
                                    interactor.oldEvent.value!!.order
                                )
                                    .add(
                                        uiEditor.item.copy(id = UUIDS.createShort()),
                                        interactor.oldEvent.value!!.order + 1
                                    )
                            }
                        }
                        finish()
                    }
                }
            }
            R.id.option_delete_event -> {
                viewModelScope.launch {
                    interactor.postEvent {
                        it.remove(uiEditor.item)
                    }
                    finish()
                }
            }
            R.id.option_clear_event -> {
                viewModelScope.launch {
                    interactor.postEvent { eventsOfDay ->
                        uiEditor.item.run {
                            eventsOfDay.update(
                                createEmpty(
                                    id, groupHeader, order, details
                                )
                            )
                        }
                    }
                    finish()
                }
            }
        }
    }

    fun onToolbarClick() {
        showListOfEventTypes.value = Pair(
            eventTypeNames,
            interactor.getDetails().type.ordinal
        )
    }

    fun onEventTypeSelect(position: Int) {
        if (position == uiEditor.item.type.ordinal)
            return
        showDetailEditor(position)
    }

    private fun showDetailEditor(position: Int) {
        toolbarTitle = when (position) {
            0 -> {
                showDetailEditor.value = R.id.eventEditorFragment
                "Редактировать урок"
            }
            1 -> {
                showDetailEditor.value = R.id.simpleEventEditorFragment
                "Редактировать событие"
            }
            2 -> {
                showDetailEditor.value = 0
                interactor.getDetails = { EmptyEventDetails() }
                interactor.validateEventDetails = { true }
                "Редактировать окно"
            }
            else -> throw IllegalStateException()
        }
    }

    init {
        uiEditor.oldItem = interactor.oldEvent.value!!
        fillFields()
        uiValidator = UIValidator.of(
            Validation(Rule { !TextUtils.isEmpty(uiEditor.item.room) })
                .sendActionResult({
                    showErrorField.setValue(
                        Pair(R.id.rl_lesson_room, true)
                    )
                }) {
                    showErrorField.setValue(
                        Pair(R.id.rl_lesson_room, false)
                    )
                },
            Validation(Rule({ interactor.validateEventDetails() }, ""))
        )
        showDetailEditor(interactor.oldEvent.value!!.type.ordinal)
    }
}