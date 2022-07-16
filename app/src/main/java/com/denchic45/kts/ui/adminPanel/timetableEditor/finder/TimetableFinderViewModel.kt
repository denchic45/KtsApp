package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.ui.UiImage
import com.denchic45.kts.domain.usecase.FindGroupByContainsNameUseCase
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.util.NetworkException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class TimetableFinderViewModel @Inject constructor(
    var eventEditorInteractor: EventEditorInteractor,
    private val interactor: TimetableFinderInteractor,
    private val findGroupByContainsNameUseCase: FindGroupByContainsNameUseCase
) : BaseViewModel() {
    val showFoundGroups = MutableSharedFlow<List<ListItem>>()

    val openEventEditor = SingleLiveData<Void>()

    private val selectedGroup = MutableSharedFlow<GroupHeader>(replay = 1)
    private val selectedDate = MutableStateFlow<LocalDate>(LocalDate.now())

    private val editEventsMode = MutableStateFlow(false)

    private val _eventsOfDayFromDataSource: SharedFlow<EventsOfDay> =
        combine(
            selectedDate,
            selectedGroup
        ) { date, courseGroup ->
            date to courseGroup
        }.flatMapLatest { (date, courseGroup) ->
            interactor.findEventsOfDayByGroup(date, courseGroup.id)
        }.shareIn(viewModelScope, SharingStarted.Lazily, replay = 1)

    private val _eventsOfDay: MutableStateFlow<EventsOfDay> =
        MutableStateFlow(EventsOfDay.createEmpty(LocalDate.now()))

    init {
        viewModelScope.launch {
            _eventsOfDayFromDataSource.combine(editEventsMode) { a, b -> a to b }
                .collect { (eventsOfDay, editMode) ->
                    if (!editMode && !savingEditedEvents)
                        _eventsOfDay.emit(eventsOfDay)
                }
        }
    }

    val eventsOfDay: StateFlow<EventsOfDayState> =
        combine(_eventsOfDay, editEventsMode)
        { eventsOfDay, enableEditMode ->
            if (enableEditMode)
                EventsOfDayState.Edit(eventsOfDay.events, eventsOfDay.date)
            else {
                EventsOfDayState.Current(eventsOfDay.events)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            EventsOfDayState.Current(emptyList())
        )

    val editTimetableOptionVisibility = SingleLiveData<Boolean>()

    private val typedGroupName = MutableSharedFlow<String>()

    private var savingEditedEvents = false
    private var foundGroupHeaders: List<GroupHeader>? = null

    fun onGroupNameType(groupName: String) {
        viewModelScope.launch {
            typedGroupName.emit(groupName)
        }
    }

    fun onGroupClick(position: Int) {
        viewModelScope.launch {
            selectedGroup.emit(foundGroupHeaders!![position])
            updateVisibilityTimetableOption()
        }
    }

    fun onDateSelect(date: LocalDate) {
        viewModelScope.launch {
            selectedDate.emit(date)
            if (selectedGroup.replayCache.isNotEmpty()) {
                editTimetableOptionVisibility.value = true
            }
        }
    }

    override fun onOptionClick(itemId: Int) {
        viewModelScope.launch {
            if (itemId == R.id.menu_timetable_edit) {
                editEventsMode.emit(true)
            }
        }
    }

    fun onActionItemClick(itemId: Int) {
        viewModelScope.launch {
            when (itemId) {
                R.id.menu_timetable_edit_save -> savingEditedEvents = true
                R.id.menu_timetable_edit_cancel -> savingEditedEvents = false
            }
            editEventsMode.emit(false)
        }
    }

    val lessonTime: Int
        get() = interactor.lessonTime

    override fun onCreateOptions() {
        super.onCreateOptions()
        updateVisibilityTimetableOption()
    }

    private fun updateVisibilityTimetableOption() {
        editTimetableOptionVisibility.value = selectedGroup.replayCache.isNotEmpty()
    }

    fun onEventEditItemEditClick(position: Int) {
        eventEditorInteractor.setEditedEvent(
            _eventsOfDay.value,
            eventsOfDay.value.events[position] as Event
        )
        openEventEditor.call()
        viewModelScope.launch {
            eventEditorInteractor.receiveEvent().apply {
                _eventsOfDay.update {
                    for (event in this.events) {
                        Log.d("lol", "_eventsOfDay: ${event.order}")
                    }
                    this
                }
            }

        }
    }

    fun onDestroyActionMode() {
        if (savingEditedEvents) {
            savingEditedEvents = false
            viewModelScope.launch {
                try {
                    interactor.updateGroupEventsOfDay(
                        _eventsOfDay.value,
                        selectedGroup.first()
                    )
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showToast(R.string.error_check_network)
                    }
                    e.printStackTrace()
                }
            }
        }
    }

    fun onEventItemMove(oldPosition: Int, targetPosition: Int) {

        viewModelScope.launch {
            _eventsOfDay.emit(
                _eventsOfDay.value.swap(oldPosition, targetPosition)
            )
        }
    }

    fun onCreateEventItemClick() {
        viewModelScope.launch {
            val order =
                if (_eventsOfDay.value.isEmpty()) 1 else _eventsOfDay.value.last().order + 1
            val createdLesson =
                Event.createEmpty(
                    groupHeader = selectedGroup.first(),
                    order = order,
                    details = Lesson.createEmpty()
                )
            eventEditorInteractor.setEditedEvent(_eventsOfDay.value, createdLesson)
            openEventEditor.call()


            _eventsOfDay.update { eventEditorInteractor.receiveEvent() }
        }
    }

    init {
        viewModelScope.launch {
            typedGroupName.filter { s -> s.length > 1 }
                .flatMapLatest { groupName -> findGroupByContainsNameUseCase(groupName) }
                .collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            foundGroupHeaders = resource.data
                            showFoundGroups.emit(resource.data
                                .map { (id, name) ->
                                    ListItem(
                                        id = id,
                                        title = name,
                                        icon = UiImage.IdImage(R.drawable.ic_group)
                                    )
                                })
                        }
                    }
                }
        }
    }

    sealed class EventsOfDayState {

        abstract val events: List<DomainModel>

        data class Current(override val events: List<Event>) :
            EventsOfDayState()

        data class Edit(private var editingEvents: List<Event>, val date: LocalDate) :
            EventsOfDayState() {

            override val events: List<DomainModel> = editingEvents + ListItem(
                id = "",
                title = "",
                type = EventAdapter.TYPE_CREATE,
                content = date
            )
        }
    }

}