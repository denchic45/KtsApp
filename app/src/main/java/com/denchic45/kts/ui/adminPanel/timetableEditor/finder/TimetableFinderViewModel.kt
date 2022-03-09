package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import androidx.lifecycle.MutableLiveData
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
    private val selectedDate = MutableSharedFlow<LocalDate>()

    private val _eventsOfDay: Flow<EventsOfDay> =
        combineTransform(
            selectedDate,
            selectedGroup
        ) { date, courseGroup ->
            emitAll(interactor.findLessonsOfGroupByDate(date, courseGroup.id))
        }

    val eventsOfDay: StateFlow<List<Event>> = _eventsOfDay.mapLatest { it.events }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val showEditedLessons = MutableLiveData<List<DomainModel>>()

    val editTimetableOptionVisibility = SingleLiveData<Boolean>()

    val enableEditMode = MutableLiveData<Boolean>()
    private val typedGroupName = MutableSharedFlow<String>()
    private val lastEvents: MutableList<Event> = ArrayList()
    private var editingEvents: List<Event> = ArrayList()

    private var saveEditedLessons = false
    private var foundGroups: List<CourseGroup>? = null
    private var selectedDate1 = LocalDate.now()
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
        if (itemId == R.id.menu_timetable_edit) {
            enableEditMode.value = true
            editingEvents = eventsOfDay.value
            lastEvents.addAll(eventsOfDay.value)
            postUpdateLessonsOfGroup()
        }
    }

    fun onActionItemClick(itemId: Int) {
        when (itemId) {
            R.id.menu_timetable_edit_save -> saveEditedLessons = true
            R.id.menu_timetable_edit_cancel -> saveEditedLessons = false
        }
        enableEditMode.value = false
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
        eventEditorInteractor.setEditedEvent(editingEvents[position], false)
        openEventEditor.call()
        viewModelScope.launch {
            eventEditorInteractor.receiveEvent()
                .let { resource ->
                    val updatedEventsOfDay =
                        EventsOfDay(selectedDate.first(), editingEvents.toMutableList())
                    when ((resource as Resource.Success).data.second) {
                        EventEditorInteractor.LESSON_CREATED -> updatedEventsOfDay.add(resource.data.first)
                        EventEditorInteractor.LESSON_EDITED -> updatedEventsOfDay.update(resource.data.first)
                        EventEditorInteractor.LESSON_REMOVED -> updatedEventsOfDay.remove(resource.data.first)
                        else -> throw IllegalStateException()
                    }
                    editingEvents = updatedEventsOfDay.events
                    postUpdateLessonsOfGroup()
                }
        }
    }

    fun onDestroyActionMode() {
        if (saveEditedLessons) {
            saveEditedLessons = false
            if (editingEvents != lastEvents) {
                viewModelScope.launch {
                    try {
                        interactor.updateGroupLessonOfDay(
                            ArrayList(editingEvents),
                            selectedDate1,
                            selectedGroup.first()
                        )
                    } catch (e: Exception) {
                        if (e is NetworkException) {
                           showToast(R.string.error_check_network)
                        }
                    }
                }
            } else {
                showEditedLessons.setValue(ArrayList<DomainModel>(lastEvents))
            }
        } else {
            showEditedLessons.setValue(ArrayList<DomainModel>(lastEvents))
        }
        lastEvents.clear()
        editingEvents = emptyList()
    }

    fun onLessonItemMove(oldPosition: Int, targetPosition: Int) {
//        val shiftedEvent = editingEvents[oldPosition]
//        val movedEvent = editingEvents[targetPosition]
        EventsOfDay(selectedDate.replayCache[0], editingEvents.toMutableList()).apply {
//            Events.swap(editingEvents, shiftedEvent, movedEvent)
            this.swap(oldPosition, targetPosition)
            editingEvents = this.events
            postUpdateLessonsOfGroup()
        }
    }

    private fun postUpdateLessonsOfGroup() {
        showEditedLessons.value = editingEvents + ListItem(
            id = "",
            title = "",
            content = selectedDate1,
            type = EventAdapter.TYPE_CREATE
        )
    }

    fun onCreateEventItemClick() {
        viewModelScope.launch {
            val order =
                if (editingEvents.isEmpty()) 1 else editingEvents[editingEvents.size - 1].order + 1
            val createdLesson =
                Event.empty(
                    group = selectedGroup.first(),
                    order = order,
                    date = selectedDate1,
                    details = Lesson.createEmpty()
                )
            eventEditorInteractor.setEditedEvent(createdLesson, true)
            openEventEditor.call()

            eventEditorInteractor.receiveEvent()
                .let { resource ->
                    if ((resource as Resource.Success).data.second == EventEditorInteractor.LESSON_CREATED) {
                        val event = resource.data.first
                        EventsOfDay(
                            selectedDate.replayCache[0],
                            editingEvents.toMutableList()
                        ).apply {
//                            Events.add(editingEvents, event)
                            this.add(event)
                            editingEvents = this.events
                            postUpdateLessonsOfGroup()
                        }
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
}