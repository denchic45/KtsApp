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
import com.denchic45.kts.utils.DatePatterns
import com.denchic45.kts.utils.toString
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
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

    val openDatePicker = SingleLiveData<Void>()
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
                    dateField.value = interactor.oldEventsOfDay.value!!.date.toString(DatePatterns.dd_MMMM)
                    orderField.value = if (interactor.isNew) (interactor.oldEventsOfDay.value!!.size + 1).toString() else event.order.toString()
                }
            }
        }
    }

    fun onDateClick() {
//        openDatePicker.call()
    }

    private fun saveChanges() {
//        if (uiEditor.isNew) uiEditor.item.id = UUID.randomUUID().toString()
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

    fun onDateSelected(selection: Long) {
        dateField.value = Instant.ofEpochMilli(selection)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .toString(DatePatterns.dd_MMMM)
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
            R.id.option_duplicate_event -> {
                viewModelScope.launch {
                    uiValidator.runValidates { saveChanges() }
                    if (uiValidator.runValidates()) {
                        interactor.postEvent {
                            it.add(uiEditor.item, interactor.oldEvent.value!!.order)
                        }
                    }
                    finish()
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
                    interactor.postEvent {
                        uiEditor.item.run {
                            it.update(
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
        if (position == uiEditor.item.type.ordinal) return
//        interactor.setEditedEvent(
//            interactor.event.copy(details = EmptyEventDetails()),
//            interactor.isNew
//        )

        showDetailEditor(position)
    }

    private fun showDetailEditor(position: Int) {
//        if (uiEditor.isNew && position == 2) {
//            title.value = "Редактировать урок"
//            showDetailEditor.value = R.id.lessonEditorFragment
//            return
//        }
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