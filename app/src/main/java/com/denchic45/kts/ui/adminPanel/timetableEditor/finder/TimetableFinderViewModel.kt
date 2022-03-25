package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

class TimetableFinderViewModel @Inject constructor(
    var eventEditorInteractor: EventEditorInteractor,
    private val interactor: TimetableFinderInteractor
) : BaseViewModel() {
    val showFoundGroups = SingleLiveData<List<ListItem>>()

    val openEventEditor = SingleLiveData<Void>()

    private val selectedGroup = MutableSharedFlow<CourseGroup>(replay = 1)
    private val selectedDate = MutableSharedFlow<LocalDate>(replay = 1)

    private val enableEditEvents = MutableStateFlow(false)

    private val _eventsOfDayFromDataSource: StateFlow<EventsOfDay> =
        combine(
            selectedDate,
            selectedGroup
        ) { date, courseGroup ->
            date to courseGroup
        }.flatMapLatest { (date, courseGroup) ->
            interactor.findLessonsOfGroupByDate(date, courseGroup.id)
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            EventsOfDay.createEmpty(LocalDate.now())
        )

    private val _eventsOfDay: MutableStateFlow<EventsOfDay> =
        MutableStateFlow(EventsOfDay.createEmpty(LocalDate.now()))

    init {
        viewModelScope.launch { _eventsOfDay.emitAll(_eventsOfDayFromDataSource) }
    }

    val eventsOfDay: StateFlow<EventsOfDayState> =
        combine(_eventsOfDay, selectedDate, enableEditEvents)
        { eventsOfDay, selectedDate, editing ->
            if (editing)
                EventsOfDayState.Edit(selectedDate, eventsOfDay.events)
            else {
                EventsOfDayState.Current(selectedDate, eventsOfDay.events)
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            EventsOfDayState.Current(LocalDate.now(), emptyList())
        )

//    val showEditedLessons = MutableLiveData<List<DomainModel>>()

    val editTimetableOptionVisibility = SingleLiveData<Boolean>()

    //    val enableEditMode = MutableLiveData<Boolean>()
    private val typedGroupName = MutableSharedFlow<String>()
//    private val lastEvents: MutableList<Event> = ArrayList()
//    private var editingEvents: List<Event> = ArrayList()

    private var saveEditedLessons = false
    private var foundGroups: List<CourseGroup>? = null

    //    private var selectedDate1 = LocalDate.now()
    fun onGroupNameType(groupName: String) {
        viewModelScope.launch {
            typedGroupName.emit(groupName)
        }
    }

    fun onGroupClick(position: Int) {
        viewModelScope.launch {
            selectedGroup.emit(foundGroups!![position])
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
                enableEditEvents.emit(true)
//                enableEditMode.value = true
//                editingEvents = eventsOfDay.value
//                lastEvents.addAll(eventsOfDay.value)
                postUpdateLessonsOfGroup()
            }
        }
    }

    fun onActionItemClick(itemId: Int) {
        viewModelScope.launch {
            when (itemId) {
                R.id.menu_timetable_edit_save -> saveEditedLessons = true
                R.id.menu_timetable_edit_cancel -> saveEditedLessons = false
            }
            enableEditEvents.emit(false)
//        enableEditMode.value = false
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
            eventEditorInteractor.receiveEvent()
                .let { resource ->
                    _eventsOfDay.update { resource }
                }
        }
    }

    fun onDestroyActionMode() {
        if (saveEditedLessons) {
            saveEditedLessons = false
//            if (editingEvents != lastEvents) {
            viewModelScope.launch {
                try {
                    interactor.updateGroupLessonOfDay(
                        _eventsOfDay.first().events,
                        selectedDate.first(),
                        selectedGroup.first()
                    )
                } catch (e: Exception) {
                    if (e is NetworkException) {
                        showToast(R.string.error_check_network)
                    }
                }
            }
//            } else {
//                showEditedLessons.setValue(ArrayList<DomainModel>(lastEvents))
//            }
        } else {
//            showEditedLessons.setValue(ArrayList<DomainModel>(lastEvents))
        }
//        lastEvents.clear()
//        editingEvents = emptyList()
    }

    fun onLessonItemMove(oldPosition: Int, targetPosition: Int) {
//        val shiftedEvent = editingEvents[oldPosition]
//        val movedEvent = editingEvents[targetPosition]


        viewModelScope.launch {
            _eventsOfDay.emit(
                _eventsOfDay.first().swap(oldPosition, targetPosition)
            )
        }


//        EventsOfDay(selectedDate.replayCache[0], editingEvents.toMutableList()).apply {
////            Events.swap(editingEvents, shiftedEvent, movedEvent)
//            this.swap(oldPosition, targetPosition)
//            editingEvents = this.events
//            postUpdateLessonsOfGroup()
//        }
    }

    private fun postUpdateLessonsOfGroup() {
//        showEditedLessons.value = editingEvents + ListItem(
//            id = "",
//            title = "",
//            content = selectedDate1,
//            type = EventAdapter.TYPE_CREATE
//        )
    }

    fun onCreateEventItemClick() {
        viewModelScope.launch {
            val order =
                if (_eventsOfDay.first().isEmpty()) 1 else _eventsOfDay.first().last().order + 1
            val createdLesson =
                Event.createEmpty(
                    group = selectedGroup.first(),
                    order = order,
                    date = selectedDate.first(),
                    details = Lesson.createEmpty()
                )
            eventEditorInteractor.setEditedEvent(_eventsOfDay.value, createdLesson)
            openEventEditor.call()

            eventEditorInteractor.receiveEvent()
                .let {
                    _eventsOfDay.update { it }
                }
        }
    }

    init {

//        eventsOfDay =
//            Transformations.map(Transformations.switchMap(selectedGroup) { groupItem: CourseGroup ->
//                interactor.findLessonsOfGroupByDate(
//                    selectedDate1,
//                    groupItem.id
//                ).asLiveData()
//            }) { eventsOfDay ->
//                if (editingEvents.isNotEmpty()) {
//                    return@map null
//                } else {
//                    return@map eventsOfDay
//                }
//            }

        viewModelScope.launch {
            typedGroupName.filter { s -> s.length > 1 }
                .flatMapLatest { groupName -> interactor.findGroupByTypedName(groupName) }
                .map { resource ->
                    foundGroups = resource
                    resource
                        .map { (id, name) ->
                            ListItem(
                                id = id,
                                title = name,
                                icon = EitherMessage.Id(R.drawable.ic_group)
                            )
                        }
                }
                .collect(showFoundGroups::postValue)
        }
    }

    sealed class EventsOfDayState(
        private val date: LocalDate,
    ) {

        abstract val events: List<DomainModel>

        data class Current(val date: LocalDate, override val events: List<Event>) :
            EventsOfDayState(date)

        data class Edit(val date: LocalDate, private var editingEvents: List<Event>) :
            EventsOfDayState(date) {

            override val events: List<DomainModel> = editingEvents + ListItem(
                id = "",
                title = "",
                content = date,
                type = EventAdapter.TYPE_CREATE
            )
        }
    }

}