package com.denchic45.kts.ui.adminPanel.timetableEditor.loader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.domain.model.Event
import com.denchic45.kts.domain.model.GroupHeader
import com.denchic45.kts.data.model.domain.GroupTimetable
import com.denchic45.kts.data.model.domain.ListItem
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.PreferenceContentItem
import com.denchic45.kts.ui.adapter.PreferenceItem
import com.denchic45.kts.ui.adapter.PreferenceSwitchItem
import com.denchic45.kts.ui.adminPanel.timetableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.login.groupChooser.GroupChooserInteractor
import com.denchic45.kts.util.NetworkException
import com.denchic45.kts.util.updated
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
    val updateEventsOfGroup = MutableSharedFlow<Pair<Int, List<List<DomainModel>>>>(replay = 1)

    val enableEditMode = MutableLiveData(false)
    val showPage = MutableLiveData<Int>()

    private val groupHeaders: MutableList<GroupHeader> = mutableListOf()

    val preferences = MutableStateFlow<List<PreferenceItem>>(
        listOf(
            PreferenceContentItem(
                id = ITEM_PUBLISH,
                title = "????????????????????????",
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
                        groupHeaders.add(groupTimetable.groupHeader)
                        groupNames.add(groupTimetable.groupHeader.name)
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
                    title = "????????????????????????",
                    icon = R.drawable.ic_send,
                    progress = false
                ), PreferenceSwitchItem(
                    id = ITEM_EDIT_MODE,
                    title = "?????????? ????????????????????????????",
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
                                title = "????????????????????",
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
                                    title = "?????????????????? ??????????????",
                                    icon = R.drawable.ic_back,
                                    progress = false
                                )
                            )
                        )

                        if (enableEditMode.value!!) {
                            enableEditMode.value = false
                            postUpdatedEventsOfGroup(positionOfCurrentTimetable)
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
            preferences.update {
                it.updated(
                    position,
                    (it[position] as PreferenceSwitchItem).copy(checked = isChecked)
                )
            }
            enableEditMode.value = isChecked
            if (groupsTimetables.isNotEmpty())
                postUpdatedTimetables()
        }
    }

    fun onEventItemEditClick(eventPosition: Int, dayOfWeek: Int) {
        val event = groupsTimetables[positionOfCurrentTimetable]
            .weekEvents[dayOfWeek]
            .events[eventPosition]

        eventEditorInteractor.setEditedEvent(
            groupsTimetables[positionOfCurrentTimetable]
                .weekEvents[dayOfWeek],
            event
        )

        receiveUpdatedEventsOfDay()
    }

    private fun receiveUpdatedEventsOfDay() {
        openEventEditor.call()
        viewModelScope.launch {
            eventEditorInteractor.receiveEvent()
                .let { eventsOfDay ->
                    groupsTimetables[positionOfCurrentTimetable] =
                        groupsTimetables[positionOfCurrentTimetable].updateEventsOfDay(eventsOfDay)

                    postUpdatedEventsOfGroup(positionOfCurrentTimetable)
                }
        }
    }

    fun onCreateEventItemClick(dayOfWeek: Int) {
        val eventsOfTheDay = groupsTimetables[positionOfCurrentTimetable].weekEvents[dayOfWeek]

        eventEditorInteractor.setEditedEvent(
            eventsOfDay = groupsTimetables[positionOfCurrentTimetable].weekEvents[dayOfWeek],
            event = Event.createEmpty(
                groupHeader = groupHeaders[positionOfCurrentTimetable],
                order = if (eventsOfTheDay.isEmpty()) 1 else eventsOfTheDay.last().order + 1
            )
        )
        receiveUpdatedEventsOfDay()
    }

    fun onEventItemMove(oldPosition: Int, targetPosition: Int, dayOfWeek: Int) {
        viewModelScope.launch {
            groupsTimetables[positionOfCurrentTimetable] =
                groupsTimetables[positionOfCurrentTimetable].run {
                    updateEventsOfDay(
                        getByDayOfWeek(dayOfWeek).swap(oldPosition, targetPosition)
                    )
                }
            postUpdatedEventsOfGroup(positionOfCurrentTimetable)
        }
    }

    private suspend fun postUpdatedEventsOfGroup(positionGroup: Int) {
        val timetable = groupsTimetables[positionGroup].toTimetableItems()
        updateEventsOfGroup.emit(Pair(positionGroup, timetable))
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
                    groupHeaders.add(group)
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