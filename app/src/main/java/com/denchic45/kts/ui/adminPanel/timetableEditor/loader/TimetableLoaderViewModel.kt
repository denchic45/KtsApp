package com.denchic45.kts.ui.adminPanel.timetableEditor.loader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.PreferenceContentItem
import com.denchic45.kts.ui.adapter.PreferenceItem
import com.denchic45.kts.ui.adapter.PreferenceSwitchItem
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.login.groupChooser.GroupChooserInteractor
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import javax.inject.Inject

class TimetableLoaderViewModel @Inject constructor(
    private val interactor: TimetableLoaderInteractor,
    private var eventEditorInteractor: EventEditorInteractor,
    private var groupChooserInteractor: GroupChooserInteractor
) : BaseViewModel() {
    val openFilePicker = SingleLiveData<Unit>()
    val showErrorDialog = SingleLiveData<String>()
    val openEventEditor = SingleLiveData<Void>()
    val openChoiceOfGroup = SingleLiveData<Void>()

    val addGroup = SingleLiveData<Unit>()
    val updateEventsOfGroup = SingleLiveData<Pair<Int, List<List<DomainModel>>>>()

    val enableEditMode = MutableLiveData(false)
    val showPage = MutableLiveData<Int>()

    private val groups: MutableList<CourseGroup> = mutableListOf()

    val preferences = MutableStateFlow<List<PreferenceItem>>(
        listOf(
            PreferenceContentItem(
                id = ITEM_PUBLISH,
                title = "Опубликовать",
                icon = R.drawable.ic_send,
                progress = true
            )
        )
    )


    val timetables = MutableSharedFlow<List<List<List<DomainModel>>>>(replay = 1)

    val tabs = MutableSharedFlow<List<String>>()

    private lateinit var firstDateOfTimetable: LocalDate
    private var positionOfCurrentTimetable = 0
    private val groupsTimetables: MutableList<GroupTimetable> = mutableListOf()
    val groupNames: MutableList<String> = mutableListOf()

    fun onLoadTimetableDocClick() {
        openFilePicker.call()
    }

    fun onSelectedFile(file: File) {
        showPage.value = PAGE_TIMETABLE

        viewModelScope.launch {
            try {
                groupsTimetables.addAll(interactor.parseDocumentTimetable(file))

                withContext(Dispatchers.Main) {
                    for (groupTimetable in groupsTimetables) {
                        firstDateOfTimetable = groupTimetable.weekEvents[0].date
                        groups.add(groupTimetable.group)
                        groupNames.add(groupTimetable.group.name)
                    }
                    postStartedTimetables()
                    postAllowEditTimetablePreferences()
                }
            } catch (throwable: Exception) {
                throwable.printStackTrace()
                showPage.value = PAGE_LOAD_DOCUMENT
                showErrorDialog.postValue(throwable.message)
            }
        }
    }

    private suspend fun postStartedTimetables() {
        postTabs()
        postUpdatedTimetables()
    }

    private suspend fun postUpdatedTimetables() {
        timetables.emit(groupTimetablesToTimetablesItems())
    }

    private suspend fun postTabs() {
        tabs.emit(groupNames)
    }

    private fun groupTimetablesToTimetablesItems(): List<List<List<DomainModel>>> {
        return groupsTimetables.map { it.toTimetableItems() }
    }

    private fun GroupTimetable.toTimetableItems(): List<List<DomainModel>> {
        val timetable = mutableListOf<List<DomainModel>>()
        for (eventsOfTheDay in this.weekEvents) {
            val listOfDayEvents = mutableListOf<DomainModel>()
            listOfDayEvents.add(
                ListItem(
                    id = "HEADER ${eventsOfTheDay.weekName}",
                    title = eventsOfTheDay.weekName,
                    content = eventsOfTheDay.date,
                    type = EventAdapter.TYPE_HEADER
                )
            )
            listOfDayEvents.addAll(eventsOfTheDay.events)
            if (enableEditMode.value!!) {
                listOfDayEvents.add(
                    ListItem(
                        id = "",
                        title = "",
                        content = eventsOfTheDay.date,
                        type = EventAdapter.TYPE_CREATE
                    )
                )
            }
            timetable.add(listOfDayEvents)
        }
        return timetable
    }

    fun onFirstDateOfNewTimetableSelect(firstDate: Long) {
        viewModelScope.launch {
            firstDateOfTimetable =
                Instant.ofEpochMilli(firstDate).atZone(ZoneId.systemDefault()).toLocalDate()
            showPage.value = PAGE_TIMETABLE
            postAllowEditTimetablePreferences()
            postStartedTimetables()
        }
    }

    private suspend fun postAllowEditTimetablePreferences() {
        preferences.emit(
            listOf(
                PreferenceContentItem(
                    id = ITEM_PUBLISH,
                    title = "Опубликовать",
                    icon = R.drawable.ic_send,
                    progress = false
                ), PreferenceSwitchItem(
                    id = ITEM_EDIT_MODE,
                    title = "Режим редактирования",
                    checked = false
                )
            )
        )
    }

    fun onPreferenceItemClick(position: Int) {
        viewModelScope.launch {
            val clickedItemId = preferences()[position]
            when (clickedItemId.id) {
                ITEM_PUBLISH -> {
                    preferences.emit(
                        listOf(
                            PreferenceContentItem(
                                id = ITEM_PUBLISH,
                                title = "Публикация",
                                icon = R.drawable.ic_send,
                                progress = true
                            )
                        )
                    )
                    try {
                        interactor.addTimetables(groupsTimetables.toList())
                        preferences.emit(
                            listOf(
                                PreferenceContentItem(
                                    id = ITEM_BACK,
                                    title = "Вернуться обратно",
                                    icon = R.drawable.ic_back,
                                    progress = false
                                )
                            )
                        )

                        if (enableEditMode.value!!) {
                            enableEditMode.value = false
                            postUpdateLessonsOfGroup(positionOfCurrentTimetable)
                        }
                    } catch (e: Exception) {
                        if (e is NetworkException) {
                            showToast(R.string.error_check_network)
                        }
                        e.printStackTrace()
                    }
                }
                ITEM_BACK -> finish()
                ITEM_SHOW -> {
                }
            }
        }
    }

    fun onPreferenceItemCheck(position: Int, isChecked: Boolean) {
        viewModelScope.launch {
            enableEditMode.value = isChecked
            if (groupsTimetables.isNotEmpty())
                postUpdatedTimetables()
        }
    }

    fun onLessonItemEditClick(eventPosition: Int, dayOfWeek: Int) {
        val event =
            groupsTimetables[positionOfCurrentTimetable].weekEvents[dayOfWeek].events[eventPosition]
        eventEditorInteractor.setEditedEvent(event, false)
        openEventEditor.call()
        viewModelScope.launch {
            eventEditorInteractor.receiveEvent()
                .let { resource ->
                    when ((resource as Resource.Success).data.second) {
                        EventEditorInteractor.LESSON_CREATED -> {
                            findLessonOfDay(
                                resource.data.first,
                                this@TimetableLoaderViewModel.positionOfCurrentTimetable
                            )
                                .ifPresent { eventsOfDay: EventsOfDay ->
                                    eventsOfDay.add(resource.data.first)
                                    postUpdateLessonsOfGroup(this@TimetableLoaderViewModel.positionOfCurrentTimetable)
                                }
                            updateEventOfGroup(positionOfCurrentTimetable, resource.data.first)
                            postUpdateLessonsOfGroup(positionOfCurrentTimetable)
                        }
                        EventEditorInteractor.LESSON_EDITED -> {
                            updateEventOfGroup(positionOfCurrentTimetable, resource.data.first)
                            postUpdateLessonsOfGroup(positionOfCurrentTimetable)
                        }
                        EventEditorInteractor.LESSON_REMOVED -> {
                            findLessonOfDay(resource.data.first, positionOfCurrentTimetable)
                                .ifPresent { eventsOfDay: EventsOfDay ->
                                    eventsOfDay.remove(resource.data.first)
                                }
                            postUpdateLessonsOfGroup(positionOfCurrentTimetable)
                        }
                    }
                }
        }
    }

    private fun updateEventOfGroup(positionGroup: Int, editedEvent: Event) {
        findLessonOfDay(editedEvent, positionGroup)
            .ifPresent { eventsOfDay: EventsOfDay ->
                eventsOfDay.events.stream()
                    .filter { oldLesson: Event -> oldLesson.id == editedEvent.id }
                    .findFirst()
                    .ifPresent {
                        eventsOfDay.update(editedEvent)
                    }
            }
    }

    fun onCreateLessonItemClick(dayOfWeek: Int) {
        val eventsOfTheDay = groupsTimetables[positionOfCurrentTimetable].weekEvents[dayOfWeek]

        val order = if (eventsOfTheDay.isEmpty()) 1 else eventsOfTheDay.last().order + 1
        val createdLesson =
            Event.empty(
                group = groups[positionOfCurrentTimetable],
                order = order,
                date = eventsOfTheDay.date
            )
        eventEditorInteractor.setEditedEvent(createdLesson, true)
        openEventEditor.call()
        viewModelScope.launch {
            eventEditorInteractor.receiveEvent()
                .let { event ->
                    if ((event as Resource.Success).data.second == EventEditorInteractor.LESSON_CREATED) {
                        val event1 = event.data.first
                        findLessonOfDay(event1, positionOfCurrentTimetable)
                            .ifPresent { eventsOfDay: EventsOfDay ->
                                eventsOfDay.add(event1)
                                postUpdateLessonsOfGroup(positionOfCurrentTimetable)
                            }
                    }
                }
        }
    }

    fun onLessonItemMove(oldPosition: Int, targetPosition: Int, dayOfWeek: Int) {
        val eventsOfDay = groupsTimetables[positionOfCurrentTimetable]
            .weekEvents[dayOfWeek]

        eventsOfDay.swap(oldPosition, targetPosition)
        postUpdateLessonsOfGroup(positionOfCurrentTimetable)
    }

    private fun postUpdateLessonsOfGroup(positionGroup: Int) {
        val timetable = groupsTimetables[positionGroup].toTimetableItems()
        updateEventsOfGroup.value = Pair(positionGroup, timetable)
    }

    private fun findLessonOfDay(lesson: Event, positionGroup: Int): Optional<EventsOfDay> {
        return groupsTimetables[positionGroup].weekEvents.stream()
            .filter { eventsOfDay: EventsOfDay -> lesson.date == eventsOfDay.date }
            .findAny()
    }

    private suspend fun preferences(): List<PreferenceItem> {
        return preferences.first()
    }

    fun onGroupTimetableSelect(position: Int) {
        positionOfCurrentTimetable = position
    }

    fun onAddGroupClick() {
        openChoiceOfGroup.call()
        viewModelScope.launch {
            groupChooserInteractor.receiveSelectedGroup()
                .let { group ->
                    groups.add(group)
                    groupNames.add(group.name)
                    groupsTimetables.add(GroupTimetable.createEmpty(group, firstDateOfTimetable))

                    viewModelScope.launch {
                        postTabs()
                        postUpdatedTimetables()
                    }
                }
        }
    }

    companion object {
        const val PAGE_LOAD_DOCUMENT = 0
        const val PAGE_TIMETABLE = 1
        const val ITEM_PUBLISH = "ITEM_PUBLISH"
        const val ITEM_BACK = "ITEM_BACK"
        private const val ITEM_SHOW = "ITEM_SHOW"
        private const val ITEM_EDIT_MODE = "ITEM_EDIT_MODE"
    }
}