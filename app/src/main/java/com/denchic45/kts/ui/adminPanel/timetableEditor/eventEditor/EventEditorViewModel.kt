package com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.EmptyEventDetails
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.Event.Companion.empty
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.uieditor.UIEditor
import com.denchic45.kts.uivalidator.Rule
import com.denchic45.kts.uivalidator.UIValidator
import com.denchic45.kts.uivalidator.Validation
import com.denchic45.kts.utils.DateFormatUtil
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class EventEditorViewModel @Inject constructor(
    private val interactor: EventEditorInteractor
) : BaseViewModel() {

    val showErrorField = MutableLiveData<Pair<Int, Boolean>>()

    val showDetailEditor = SingleLiveData<Int>()

    val dateField = MutableLiveData<String>()

    val orderField = SingleLiveData("")

    val roomField = SingleLiveData("")

    val showListOfEventTypes = SingleLiveData<Pair<Array<CharSequence>, Int>>()

    val openDatePicker = SingleLiveData<Void>()

    val title = MutableLiveData("Редактировать урок")
    private val uiValidator: UIValidator
    private val uiEditor: UIEditor<Event> = UIEditor(interactor.isNew) {
        interactor.oldEvent.value!!.copy(
            order = orderField.value!!.toInt(),
            room = roomField.value!!,
            details = interactor.getDetails()
        )
    }
    private val eventTypeNames = arrayOf<CharSequence>("Урок", "Другое событие", "Окно")
    private fun fillFields() {
        viewModelScope.launch {
            interactor.observeOldEvent().collect {
                it?.apply {
                    if (!interactor.isNew) {
                        roomField.value = room!!
                    }
                    dateField.value =
                        DateFormatUtil.convertDateToString(date, DateFormatUtil.dd_MMMM)
                    orderField.value = order.toString()
                }
            }
        }
    }

    fun onDateClick() {
//        openDatePicker.call()
    }

    private fun saveChanges() {
        if (uiEditor.isNew) uiEditor.item.id = UUID.randomUUID().toString()
        interactor.postEvent(
            Resource.Success(
                uiEditor.item to
                        if (uiEditor.isNew) EventEditorInteractor.LESSON_CREATED else EventEditorInteractor.LESSON_EDITED
            )
        )
        finish.call()
    }

    fun onDateSelected(selection: Long) {
        dateField.value =
            DateFormatUtil.convertDateToString(Date(selection), DateFormatUtil.dd_MMMM)
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

    fun onOptionClick(itemId: Int) {
        if (itemId == R.id.option_duplicate_lesson) {
            uiValidator.runValidates { saveChanges() }
            if (uiValidator.runValidates()) {
                val duplicatedLesson = uiEditor.item.copy(
                    id = UUID.randomUUID().toString(),
                    order = uiEditor.item.order + 1
                )
                interactor.postEvent(
                    Resource.Success(
                        duplicatedLesson to
                                EventEditorInteractor.LESSON_CREATED
                    )
                )
            }
//            finish.call() # может нужен
        } else if (itemId == R.id.option_delete_lesson) {
            interactor.postEvent(Resource.Success(uiEditor.item to EventEditorInteractor.LESSON_REMOVED))
            finish.call()
        } else if (itemId == R.id.option_clear_lesson) {
            interactor.postEvent(
                Resource.Success(
                    empty(
                        uiEditor.item.id,
                        uiEditor.item.group,
                        uiEditor.item.order,
                        uiEditor.item.date
                    ) to EventEditorInteractor.LESSON_EDITED
                )
            )
            finish.call()
        }
    }

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
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
        when (position) {
            0 -> {
                title.value = "Редактировать урок"
                showDetailEditor.setValue(R.id.lessonEditorFragment)
            }
            1 -> {
                title.value = "Редактировать событие"
                showDetailEditor.setValue(R.id.simpleEventEditorFragment)
            }
            2 -> {
                title.value = "Редактировать окно"
                showDetailEditor.value = 0
                interactor.getDetails = { EmptyEventDetails() }
                interactor.validateEventDetails = { true }
            }
        }
    }

    init {
        uiEditor.oldItem = interactor.oldEvent.value
        fillFields()
        uiValidator = UIValidator.of(
            Validation(Rule { uiEditor.item.order != -1 })
                .sendActionResult({
                    showErrorField.setValue(
                        Pair(
                            R.id.rl_lesson_order,
                            true
                        )
                    )
                }) {
                    showErrorField.setValue(
                        Pair(R.id.rl_lesson_order, false)
                    )
                },
            Validation(Rule { !TextUtils.isEmpty(uiEditor.item.room) })
                .sendActionResult({
                    showErrorField.setValue(
                        Pair(
                            R.id.rl_lesson_room,
                            true
                        )
                    )
                }) {
                    showErrorField.setValue(
                        Pair(R.id.rl_lesson_room, false)
                    )
                },
            Validation(Rule({ interactor.validateEventDetails() }, "qwe"))
        )
        showDetailEditor(interactor.oldEvent.value!!.type.ordinal)
    }
}