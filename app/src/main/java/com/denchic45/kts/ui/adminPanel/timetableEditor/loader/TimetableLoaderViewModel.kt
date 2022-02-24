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
import com.denchic45.kts.ui.adminPanel.timetableEditor.TimetableEditorInteractor
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.login.choiceOfGroup.ChoiceOfGroupInteractor
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.Dispatchers
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
    private val interactor: TimetableLoaderInteractor
) : BaseViewModel() {

    val openFilePicker = SingleLiveData<Unit>()
    val showErrorDialog = SingleLiveData<String>()
    val openLessonEditor = SingleLiveData<Void>()
    val openChoiceOfGroup = SingleLiveData<Void>()

    val addGroup = SingleLiveData<Unit>()
    val updateEventsOfGroup = SingleLiveData<Pair<Int, MutableList<DomainModel>>>()

    val enableEditMode = MutableLiveData(false)
    val showPage = MutableLiveData<Int>()

    //    val showTimetable =
//        MutableLiveData<Pair<MutableList<String>, MutableList<MutableList<DomainModel>>>>()
    private val groups: MutableList<CourseGroup> = mutableListOf()
    private var weekDays =
        arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")


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
    val timetables = MutableStateFlow(TimetablesState(emptyList(), emptyList()))

    @Inject
    lateinit var eventEditorInteractor: EventEditorInteractor

    @Inject
    lateinit var timetableEditorInteractor: TimetableEditorInteractor

    @Inject
    lateinit var choiceOfGroupInteractor: ChoiceOfGroupInteractor
    private lateinit var firstDateOfTimetable: LocalDate
    private var positionOfGroup = 0
    private lateinit var groupsTimetables: List<GroupTimetable>
    private val groupNames: MutableList<String> = mutableListOf()

    fun onLoadTimetableDocClick() {
        openFilePicker.call()
    }

    fun onSelectedFile(file: File) {
        showPage.value = PAGE_TIMETABLE

        viewModelScope.launch(Dispatchers.Default) {
            try {
                groupsTimetables = interactor.parseDocumentTimetable(file)

                withContext(Dispatchers.Main) {
                    this@TimetableLoaderViewModel.groupsTimetables =
                        groupsTimetables.toMutableList()

//                    val timetable: MutableList<MutableList<DomainModel>> = ArrayList()
                    for (groupTimetable in groupsTimetables) {
                        firstDateOfTimetable = groupTimetable.weekEvents[0].date
                        groups.add(groupTimetable.group)
                        groupNames.add(groupTimetable.group.name)
//                        timetable.add(addHeadersInLessons(groupTimetable))
                    }
//                    showTimetable.value = Pair(groupNames, timetable)
//                    postTimetables()
                    postAllowEditTimetablePreferences()
                }
            } catch (throwable: Exception) {
                throwable.printStackTrace()
                showPage.value = PAGE_LOAD_DOCUMENT
                showErrorDialog.postValue(throwable.message)
//                showTimetable.setValue(Pair(mutableListOf(), mutableListOf()))
            }


            postTimetables()
        }
    }

    private suspend fun postTimetables() {
        val timetableList = mutableListOf<MutableList<DomainModel>>()
        for (groupTimetable in groupsTimetables) {
            val timetable = mutableListOf<DomainModel>()
            for (eventsOfTheDay in groupTimetable.weekEvents) {
                timetable.add(
                    ListItem(
                        id = "",
                        title = eventsOfTheDay.weekName,
                        content = eventsOfTheDay.date,
                        type = EventAdapter.TYPE_HEADER
                    )
                )
                timetable.addAll(eventsOfTheDay.events)
                if (enableEditMode.value!!) {
                    timetable.add(ListItem(id = "", title = "", content = eventsOfTheDay.date, type = EventAdapter.TYPE_CREATE))
                }
            }
            timetableList.add(timetable)
        }

        timetables.emit(
            TimetablesState(
                groupNames,
              timetableList
            )
        )
    }

    fun onFirstDateOfNewTimetableSelect(firstDate: Long) {
        viewModelScope.launch {
            firstDateOfTimetable =
                Instant.ofEpochMilli(firstDate).atZone(ZoneId.systemDefault()).toLocalDate()
            showPage.value = PAGE_TIMETABLE
            postAllowEditTimetablePreferences()
            postTimetables()
//            showTimetable.value = Pair(ArrayList(), ArrayList())
            groupsTimetables = ArrayList()
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

    private fun addHeadersInLessons(groupTimetable: GroupTimetable): MutableList<DomainModel> {
        val timetable: MutableList<DomainModel> = ArrayList()
        for (i in weekDays.indices) {
            val eventsOfTheDay = groupTimetable.weekEvents[i]
            timetable.add(
                ListItem(
                    id = "",
                    title = weekDays[i],
                    content = eventsOfTheDay.date,
                    type = EventAdapter.TYPE_HEADER
                )
            )
            timetable.addAll(groupTimetable.weekEvents[i].events)
        }
        return timetable
    }

    private fun addCreationItemsIfNecessary(timetable: MutableList<DomainModel>) {
        if (!enableEditMode.value!!) {
            return
        }
        var i = 1
        while (i < timetable.size) {
            val item = timetable[i]
            if (item is ListItem && item.type == EventAdapter.TYPE_HEADER) {
                val lastItem = timetable[i - 1]
                val date =
                    if (lastItem is Event) lastItem.date else ((lastItem as ListItem).content as Date?)!!
                timetable.add(
                    i,
                    ListItem(id = "", title = "", content = date, type = EventAdapter.TYPE_CREATE)
                )
                i++
            }
            i++
        }
        val lastItem = timetable[timetable.size - 1]
        val date =
            if (lastItem is Event) lastItem.date else ((lastItem as ListItem).content as Date?)!!
        timetable.add(
            timetable.size,
            ListItem(id = "", title = "", content = date, type = EventAdapter.TYPE_CREATE)
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
                        interactor.addLessonsOfWeek(groupsTimetables.toList())
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
                            postUpdateLessonsOfGroup(positionOfGroup)
                        }
                    } catch (e: Exception) {
                        if (e is NetworkException) {
                            showMessageRes.value = R.string.error_check_network
                        }
                        e.printStackTrace()
                    }
                }
                ITEM_BACK -> finish.call()
                ITEM_SHOW -> {
                }
            }
        }
    }

    fun onPreferenceItemCheck(position: Int, isChecked: Boolean) {
        viewModelScope.launch {
            enableEditMode.value = isChecked
            if (groupsTimetables.isNotEmpty())
                postUpdateLessonsOfGroup(positionOfGroup)
        }
    }

    fun onLessonItemEditClick(positionLesson: Int, positionGroup: Int) {
//        val event = showTimetable.value!!.second[positionGroup][positionLesson] as Event
        val event = groupsTimetables[positionGroup].findEventByAbsolutelyPosition(positionLesson)
        eventEditorInteractor.setEditedEvent(event, false)
        openLessonEditor.call()
        eventEditorInteractor.observeEvent()
            .subscribe { resource ->
                when ((resource as Resource.Success).data.second) {
                    EventEditorInteractor.LESSON_CREATED -> {
                        findLessonOfDay(resource.data.first, positionOfGroup)
                            .ifPresent { eventsOfTheDay: EventsOfTheDay ->
                                eventsOfTheDay.add(resource.data.first)
                                postUpdateLessonsOfGroup(positionOfGroup)
                            }
                        updateLessonOfGroup(positionGroup, resource.data.first)
                        postUpdateLessonsOfGroup(positionGroup)
                    }
                    EventEditorInteractor.LESSON_EDITED -> {
                        updateLessonOfGroup(positionGroup, resource.data.first)
                        postUpdateLessonsOfGroup(positionGroup)
                    }
                    EventEditorInteractor.LESSON_REMOVED -> {
                        findLessonOfDay(resource.data.first, positionGroup)
                            .ifPresent { eventsOfTheDay: EventsOfTheDay ->
                                eventsOfTheDay.remove(resource.data.first)
                            }
                        postUpdateLessonsOfGroup(positionGroup)
                    }
                }
            }
    }

    private fun updateLessonOfGroup(positionGroup: Int, editedLesson: Event) {
        findLessonOfDay(editedLesson, positionGroup)
            .ifPresent { eventsOfTheDay: EventsOfTheDay ->
                eventsOfTheDay.events.stream()
                    .filter { oldLesson: Event -> oldLesson.id == editedLesson.id }
                    .findFirst()
                    .ifPresent {
                        eventsOfTheDay.update(editedLesson)
                    }
            }
    }

    fun onCreateLessonItemClick(position: Int) {
        val (_, _, _, _, content) = groupsTimetables[positionOfGroup].findEventByAbsolutelyPosition(
            position
        )
        val lastItem = groupsTimetables[positionOfGroup].lastEvent
        val order = lastItem?.order?.plus(1) ?: 1
        val createdLesson =
            Event.empty(group = groups[positionOfGroup], order = order, date = content as LocalDate)
        eventEditorInteractor.setEditedEvent(createdLesson, true)
        openLessonEditor.call()
        eventEditorInteractor.observeEvent()
            .subscribe { event ->
                if ((event as Resource.Success).data.second == EventEditorInteractor.LESSON_CREATED) {
                    val event1 = event.data.first
                    findLessonOfDay(event1, positionOfGroup)
                        .ifPresent { eventsOfTheDay: EventsOfTheDay ->
                            eventsOfTheDay.add(event1)
                            postUpdateLessonsOfGroup(positionOfGroup)
                        }
                }
            }
    }

    fun onLessonItemMove(positionOfCGroup: Int, oldPosition: Int, targetPosition: Int) {
        val lessonsOfGroup = timetables.value.groupEvents[positionOfCGroup]
        val shiftedLesson = lessonsOfGroup[oldPosition] as Event
        val movedLesson = lessonsOfGroup[targetPosition] as Event
        findLessonOfDay(shiftedLesson, positionOfCGroup)
            .ifPresent { eventsOfTheDay: EventsOfTheDay ->
                if (shiftedLesson.date == movedLesson.date) {
                    eventsOfTheDay.swap(shiftedLesson, movedLesson)
                    postUpdateLessonsOfGroup(positionOfCGroup)
                }
            }
    }

    private fun postUpdateLessonsOfGroup(positionGroup: Int) {
        viewModelScope.launch { postTimetables() }
        val listOfGroupLessons = timetables.value.groupEvents.toMutableList()
        val timetable = addHeadersInLessons(
            groupsTimetables[positionGroup]
        )
        addCreationItemsIfNecessary(timetable)
        listOfGroupLessons[positionGroup] = timetable
        updateEventsOfGroup.value = Pair(positionGroup, timetable)
    }

    private fun findLessonOfDay(lesson: Event, positionGroup: Int): Optional<EventsOfTheDay> {
        return groupsTimetables[positionGroup].weekEvents.stream()
            .filter { eventsOfTheDay: EventsOfTheDay -> lesson.date == eventsOfTheDay.date }
            .findAny()
    }

    private suspend fun preferences(): List<PreferenceItem> {
        return preferences.first()
    }

    fun onGroupSelect(position: Int) {
        positionOfGroup = position
    }

    fun onAddGroupClick() {
        openChoiceOfGroup.call()
        choiceOfGroupInteractor.observeSelectedGroup()
            .subscribe { group ->
                groups.add(group)
                groupNames.add(group.name)

                viewModelScope.launch { postTimetables() }

//                showTimetable.value!!.first.add(group.name)
//                groupsTimetables.add(
//                    GroupTimetable.createEmpty(group, firstDateOfTimetable)
//                )
//                showTimetable.value!!.second.add(
//                    addHeadersInLessons(
//                        groupsTimetables[groupsTimetables.size - 1]
//                    )
//                )
//                showAddedGroup.call()
            }
    }

    data class TimetablesState(
        val groupNames: List<String>,
        val groupEvents: List<List<DomainModel>>
    )

    companion object {
        const val PAGE_LOAD_DOCUMENT = 0
        const val PAGE_TIMETABLE = 1
        const val ITEM_PUBLISH = "ITEM_PUBLISH"
        const val ITEM_BACK = "ITEM_BACK"
        private const val ITEM_SHOW = "ITEM_SHOW"
        private const val ITEM_EDIT_MODE = "ITEM_EDIT_MODE"
    }
}