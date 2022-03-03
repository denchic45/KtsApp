package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import androidx.lifecycle.*
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

class TimetableFinderViewModel @Inject constructor(
    var eventEditorInteractor: EventEditorInteractor,
    private val interactor: TimetableFinderInteractor
) : BaseViewModel() {

    val showFoundGroups = SingleLiveData<List<ListItem>>()


    val openEventEditor = SingleLiveData<Void>()


    val showLessonsOfGroupByDate: LiveData<List<Event>>


    val showEditedLessons = MutableLiveData<List<DomainModel>>()


    val editTimetableOptionVisibility = SingleLiveData<Boolean>()


    val enableEditMode = MutableLiveData<Boolean>()
    private val typedGroupName = MutableSharedFlow<String>()
    private val selectedGroup = MutableLiveData<CourseGroup>()
    private val lastEvents: MutableList<Event> = ArrayList()
    private val editingEvents: MutableList<Event> = ArrayList()

    private var saveEditedLessons = false
    private var foundGroups: List<CourseGroup>? = null
    private var selectedDate = LocalDate.now()
    fun onGroupNameType(groupName: String) {
        viewModelScope.launch {
            typedGroupName.emit(groupName)
        }
    }

    fun onGroupClick(position: Int) {
        selectedGroup.value = foundGroups!![position]
        updateVisibilityTimetableOption()
    }

    fun onDateSelect(date: LocalDate) {
        selectedDate = date
        if (selectedGroup.value != null) {
            selectedGroup.value = selectedGroup.value
            editTimetableOptionVisibility.value = true
        }
    }

    override fun onOptionClick(itemId: Int) {
        if (itemId == R.id.menu_timetable_edit) {
            enableEditMode.value = true
            editingEvents.addAll(
                showLessonsOfGroupByDate.value!!
            )
            lastEvents.addAll(showLessonsOfGroupByDate.value!!)
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
        editTimetableOptionVisibility.value = selectedGroup.value != null
    }

    fun onLessonItemEditClick(position: Int) {
        eventEditorInteractor.setEditedEvent(editingEvents[position], false)
        openEventEditor.call()
        eventEditorInteractor.observeEvent()
            .subscribe { resource ->
                when ((resource as Resource.Success).data.second) {
                    EventEditorInteractor.LESSON_CREATED -> Events.add(
                        editingEvents,
                        resource.data.first
                    )
                    EventEditorInteractor.LESSON_EDITED -> Events.update(
                        editingEvents,
                        resource.data.first
                    )
                    EventEditorInteractor.LESSON_REMOVED -> Events.remove(
                        editingEvents,
                        resource.data.first
                    )
                }
                postUpdateLessonsOfGroup()
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
                            selectedDate,
                            selectedGroup.value!!
                        )
                    } catch (e: Exception) {
                        if (e is NetworkException) {
                            showMessageRes.value = R.string.error_check_network
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
        editingEvents.clear()
    }

    fun onLessonItemMove(oldPosition: Int, targetPosition: Int) {
        val shiftedEvent = editingEvents[oldPosition]
        val movedEvent = editingEvents[targetPosition]
        Events.swap(editingEvents, shiftedEvent, movedEvent)
        postUpdateLessonsOfGroup()
    }

    private fun postUpdateLessonsOfGroup() {
        showEditedLessons.value = editingEvents + ListItem(
            id = "",
            title = "",
            content = selectedDate,
            type = EventAdapter.TYPE_CREATE
        )
    }

    fun onCreateEventItemClick() {
        val order =
            if (editingEvents.isEmpty()) 1 else editingEvents[editingEvents.size - 1].order + 1
        val createdLesson =
            Event.empty(
                group = selectedGroup.value!!,
                order = order,
                date = selectedDate,
                details = Lesson.createEmpty()
            )
        eventEditorInteractor.setEditedEvent(createdLesson, true)
        openEventEditor.call()
        eventEditorInteractor.observeEvent()
            .subscribe { resource ->
                if ((resource as Resource.Success).data.second == EventEditorInteractor.LESSON_CREATED) {
                    val event = resource.data.first
                    Events.add(editingEvents, event)
                    postUpdateLessonsOfGroup()
                }
            }
    }

    init {
        showLessonsOfGroupByDate =
            Transformations.map(Transformations.switchMap(selectedGroup) { groupItem ->
                interactor.findLessonsOfGroupByDate(
                    selectedDate,
                    groupItem!!.id
                ).asLiveData()
            }) { input: List<Event> ->
                if (editingEvents.isNotEmpty()) {
                    return@map null
                } else {
                    return@map input
                }
            }
        viewModelScope.launch {
            typedGroupName.filter { s -> s.length > 1 }
                .flatMapLatest { groupName -> interactor.findGroupByTypedName(groupName) }
                .map { resource ->
                    foundGroups = (resource as Resource.Success).data
                    resource.data.stream()
                        .map { (id, name) ->
                            ListItem(
                                id = id,
                                title = name,
                                icon = EitherResource.Id(R.drawable.ic_group)
                            )
                        }
                        .collect(Collectors.toList())
                }
                .collect(showFoundGroups::postValue)
        }
    }
}