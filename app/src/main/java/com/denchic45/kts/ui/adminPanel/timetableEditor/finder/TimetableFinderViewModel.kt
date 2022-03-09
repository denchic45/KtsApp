package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
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

    private val _eventsOfDay1: MutableStateFlow<EventsOfDay> =
        MutableStateFlow(EventsOfDay.createEmpty(LocalDate.now()))

    init {
        viewModelScope.launch { _eventsOfDay1.emitAll(_eventsOfDayFromDataSource) }
    }

    val eventsOfDay: StateFlow<EventsOfDayState> =
        combine(_eventsOfDay1, selectedDate, enableEditEvents)
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

    fun onLessonItemEditClick(position: Int) {
        eventEditorInteractor.setEditedEvent(eventsOfDay.value.events[position] as Event, false)
        openEventEditor.call()
        viewModelScope.launch {
            eventEditorInteractor.receiveEvent()
                .let { resource ->
//                    val updatedEventsOfDay =
//                        EventsOfDay(selectedDate.first(), editingEvents.toMutableList())
                    when ((resource as Resource.Success).data.second) {
                        EventEditorInteractor.LESSON_CREATED -> {

                            _eventsOfDay1.emit(
                                _eventsOfDay1.first().add(resource.data.first)
                            )

//                            _eventsOfDayFromDataSource.value.add(resource.data.first)
                        }
                        EventEditorInteractor.LESSON_EDITED -> {

                            _eventsOfDay1.emit(
                                _eventsOfDay1.first().update(resource.data.first)
                            )

//                            _eventsOfDayFromDataSource.value.update(
//                                resource.data.first
//                            )
                        }
                        EventEditorInteractor.LESSON_REMOVED -> {

                            _eventsOfDay1.emit(
                                _eventsOfDay1.first().remove(resource.data.first)
                            )

//                            _eventsOfDayFromDataSource.value.remove(
//                                resource.data.first
//                            )
                        }
                        else -> throw IllegalStateException()
                    }
//                    editingEvents = updatedEventsOfDay.events
//                    postUpdateLessonsOfGroup()
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
                        _eventsOfDay1.first().events,
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
            _eventsOfDay1.emit(
                _eventsOfDay1.first().swap(oldPosition, targetPosition)
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
                if (_eventsOfDay1.first().isEmpty()) 1 else _eventsOfDay1.first().last().order + 1
            val createdLesson =
                Event.empty(
                    group = selectedGroup.first(),
                    order = order,
                    date = selectedDate.first(),
                    details = Lesson.createEmpty()
                )
            eventEditorInteractor.setEditedEvent(createdLesson, true)
            openEventEditor.call()

            eventEditorInteractor.receiveEvent()
                .let { resource ->
                    if ((resource as Resource.Success).data.second == EventEditorInteractor.LESSON_CREATED) {
                        val event = resource.data.first
//                        EventsOfDay(
//                            selectedDate.replayCache[0],
//                            editingEvents.toMutableList()
//                        ).apply {
////                            Events.add(editingEvents, event)
//                            this.add(event)
//                            editingEvents = this.events
//                            postUpdateLessonsOfGroup()
//                        }

                        _eventsOfDay1.emit(
                            _eventsOfDay1.first().add(event)
                        )
                    }
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
                                icon = EitherResource.Id(R.drawable.ic_group)
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