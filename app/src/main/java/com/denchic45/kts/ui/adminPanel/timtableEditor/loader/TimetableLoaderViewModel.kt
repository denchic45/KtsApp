package com.denchic45.kts.ui.adminPanel.timtableEditor.loader

import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.rx.AsyncTransformer
import com.denchic45.kts.ui.adapter.EventAdapter
import com.denchic45.kts.ui.adapter.ItemAdapter
import com.denchic45.kts.ui.adminPanel.timtableEditor.TimetableEditorInteractor
import com.denchic45.kts.ui.adminPanel.timtableEditor.eventEditor.EventEditorInteractor
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.ui.login.choiceOfGroup.ChoiceOfGroupInteractor
import com.denchic45.kts.utils.Events
import com.denchic45.kts.utils.NetworkException
import org.apache.commons.lang3.time.DateUtils
import org.jetbrains.annotations.Contract
import java.io.File
import java.util.*
import javax.inject.Inject

class TimetableLoaderViewModel @Inject constructor(
    private val interactor: TimetableLoaderInteractor
) : BaseViewModel() {

    val openFilePicker = SingleLiveData<Unit>()
    val allowEditTimetable = SingleLiveData<Void>()
    val showPublishingTimetable = SingleLiveData<Void>()
    val enableEditMode = MutableLiveData(false)
    val showAddedGroup = SingleLiveData<Void>()
    val showDone = SingleLiveData<Void>()
    val showErrorDialog = SingleLiveData<String>()
    val openLessonEditor = SingleLiveData<Void>()
    val openChoiceOfGroup = SingleLiveData<Void>()
    val updateLessonsOfGroup = SingleLiveData<Pair<Int, MutableList<DomainModel>>>()
    val showPreferenceList: MutableLiveData<MutableList<ListItem>> = MutableLiveData(
        ArrayList(
            listOf(
                ListItem(
                    id = ITEM_PUBLISH,
                    title = "Опубликовать",
                    content = ItemAdapter.PAYLOAD.SHOW_LOADING,
                    type = ItemAdapter.TYPE_PROGRESS,
                    icon = EitherResource.Id(R.drawable.ic_send)
                )
            )
        )
    )

    val showPage = MutableLiveData<Int>()
    val showTimetable =
        MutableLiveData<Pair<MutableList<String>, MutableList<MutableList<DomainModel>>>>()
    private val groups: MutableList<CourseGroup> = mutableListOf()
    var weekDays = arrayOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота")

    @Inject
    lateinit var eventEditorInteractor: EventEditorInteractor

    @Inject
    lateinit var timetableEditorInteractor: TimetableEditorInteractor

    @Inject
    lateinit var choiceOfGroupInteractor: ChoiceOfGroupInteractor
    private var firstDateOfTimetable: Date? = null
    private var positionOfGroup = 0
    private var groupWeekLessonsList: MutableList<GroupWeekLessons>? = null
    fun onLoadTimetableDocClick() {
        openFilePicker.call()
    }

    fun onSelectedFile(file: File) {
        showPage.value = PAGE_TIMETABLE
        interactor.parseDocumentTimetable(file)
            .compose(AsyncTransformer())
            .subscribe({ groupWeekLessonsList ->
                this.groupWeekLessonsList = groupWeekLessonsList.toMutableList()
                val groupNames: MutableList<String> = mutableListOf()
                val timetable: MutableList<MutableList<DomainModel>> = ArrayList()
                for (groupWeekLessons in groupWeekLessonsList) {
                    firstDateOfTimetable = groupWeekLessons.weekLessons[0].date
                    groups.add(groupWeekLessons.group)
                    groupNames.add(groupWeekLessons.group.name)
                    timetable.add(addHeadersInLessons(groupWeekLessons))
                }
                showTimetable.value = Pair(groupNames, timetable)
                postAllowEditTimetable()
            }) { throwable: Throwable ->
                throwable.printStackTrace()
                showPage.value = PAGE_LOAD_DOCUMENT
                showErrorDialog.postValue(throwable.message)
                showTimetable.setValue(Pair(mutableListOf(), mutableListOf()))
            }
    }

    fun onFirstDateOfNewTimetableSelect(firstDate: Long?) {
        firstDateOfTimetable = Date(firstDate!!)
        showPage.value = PAGE_TIMETABLE
        postAllowEditTimetable()
        showTimetable.value =
            Pair(
                ArrayList(),
                ArrayList()
            )
        groupWeekLessonsList = ArrayList()
    }

    private fun postAllowEditTimetable() {
        preferenceList.add(
            ListItem(
                id = ITEM_EDIT_MODE,
                title = "Режим редактирования",
                type = ItemAdapter.TYPE_SWITCH
            )
        )
        preferenceList[0].content = ItemAdapter.PAYLOAD.SHOW_IMAGE
        allowEditTimetable.call()
    }

    private fun addHeadersInLessons(groupWeekLessons: GroupWeekLessons): MutableList<DomainModel> {
        val timetable: MutableList<DomainModel> = ArrayList()
        for (i in weekDays.indices) {
            val eventsOfTheDay = groupWeekLessons.weekLessons[i]
            timetable.add(
                ListItem(
                    id = "",
                    title = weekDays[i],
                    content = eventsOfTheDay.date,
                    type = EventAdapter.TYPE_HEADER
                )
            )
            timetable.addAll(groupWeekLessons.weekLessons[i].events)
        }
        return timetable
    }

    @Contract(pure = true)
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
        val (clickedItemUuid) = preferenceList[position]
        val firstItem = preferenceList[0]
        when (clickedItemUuid) {
            ITEM_PUBLISH -> {
                firstItem.content = ItemAdapter.PAYLOAD.SHOW_LOADING
                firstItem.title = "Публикация"
                showPublishingTimetable.call()
                interactor.addLessonsOfWeek(groupWeekLessonsList!!.toList())
                    .subscribe({
                        showPreferenceList.value = mutableListOf(
                            ListItem(
                                id = ITEM_BACK,
                                title = "Вернуться обратно",
                                type = ItemAdapter.TYPE_VIEW,
                                icon = EitherResource.Id(R.drawable.ic_back)
                            )
                        )
                        if (enableEditMode.value!!) {
                            enableEditMode.value = false
                            postUpdateLessonsOfGroup(positionOfGroup)
                        }
                    }) { throwable: Throwable ->
                        if (throwable is NetworkException) {
                            showMessageRes.value = R.string.error_check_network
                        }
                        throwable.printStackTrace()
                    }
            }
            ITEM_BACK -> finish.call()
            ITEM_SHOW -> {
            }
        }
    }

    fun onPreferenceItemCheck(position: Int, isChecked: Boolean) {
        preferenceList[position].content = isChecked
        enableEditMode.value = isChecked
        postUpdateLessonsOfGroup(positionOfGroup)
    }

    fun onLessonItemEditClick(positionLesson: Int, positionGroup: Int) {
        val event = showTimetable.value!!.second[positionGroup][positionLesson] as Event
        eventEditorInteractor.setEditedEvent(event, false)
        openLessonEditor.call()
        eventEditorInteractor.observeEvent()
            .subscribe { resource ->
                when ((resource as Resource.Success).data.second) {
                    EventEditorInteractor.LESSON_CREATED -> {
                        findLessonOfDay(resource.data.first, positionOfGroup)
                            .ifPresent { eventsOfTheDay: EventsOfTheDay ->
                                Events.add(eventsOfTheDay.events, resource.data.first)
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
                                Events.remove(
                                    eventsOfTheDay.events,
                                    resource.data.first
                                )
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
                    .ifPresent { oldLesson: Event? ->
                        Events.update(
                            eventsOfTheDay.events,
                            editedLesson
                        )
                    }
            }
    }

    fun onCreateLessonItemClick(position: Int) {
        val (_, _, _, _, content) = showTimetable.value!!.second[positionOfGroup][position] as ListItem
        val lastItem = showTimetable.value!!.second[positionOfGroup][position - 1]
        val order = if (lastItem is Event) lastItem.order + 1 else 1
        val createdLesson =
            Event.empty(group = groups[positionOfGroup], order = order, date = content as Date)
        eventEditorInteractor.setEditedEvent(createdLesson, true)
        openLessonEditor.call()
        eventEditorInteractor.observeEvent()
            .subscribe { event ->
                if ((event as Resource.Success).data.second == EventEditorInteractor.LESSON_CREATED) {
                    val event1 = event.data.first
                    findLessonOfDay(event1, positionOfGroup)
                        .ifPresent { eventsOfTheDay: EventsOfTheDay ->
                            Events.add(eventsOfTheDay.events, event1)
                            postUpdateLessonsOfGroup(positionOfGroup)
                        }
                }
            }
    }

    fun onLessonItemMove(positionOfCGroup: Int, oldPosition: Int, targetPosition: Int) {
        val lessonsOfGroup = showTimetable.value!!.second[positionOfCGroup]
        val shiftedLesson = lessonsOfGroup[oldPosition] as Event
        val movedLesson = lessonsOfGroup[targetPosition] as Event
        findLessonOfDay(shiftedLesson, positionOfCGroup)
            .ifPresent { eventsOfTheDay: EventsOfTheDay ->
                if (shiftedLesson.date == movedLesson.date) {
                    val events = eventsOfTheDay.events
                    Events.swap(events, shiftedLesson, movedLesson)
                    postUpdateLessonsOfGroup(positionOfCGroup)
                }
            }
    }

    private fun postUpdateLessonsOfGroup(positionGroup: Int) {
        val listOfGroupLessons = showTimetable.value!!.second
        val timetable = addHeadersInLessons(
            groupWeekLessonsList!![positionGroup]
        )
        addCreationItemsIfNecessary(timetable)
        listOfGroupLessons.set(positionGroup, timetable)
        updateLessonsOfGroup.value = Pair(positionGroup, timetable)
    }

    private fun findLessonOfDay(lesson: Event, positionGroup: Int): Optional<EventsOfTheDay> {
        return groupWeekLessonsList!![positionGroup].weekLessons.stream()
            .filter { eventsOfTheDay: EventsOfTheDay -> lesson.date == eventsOfTheDay.date }
            .findAny()
    }

    private val preferenceList: MutableList<ListItem>
        get() = showPreferenceList.value!!

    fun onGroupSelect(position: Int) {
        positionOfGroup = position
    }

    fun onAddGroupClick() {
        openChoiceOfGroup.call()
        choiceOfGroupInteractor.observeSelectedGroup()
            .subscribe { group ->
                groups.add(group)
                showTimetable.value!!.first.add(group.name)
                groupWeekLessonsList!!.add(
                    GroupWeekLessons(
                        group, ArrayList(
                            Arrays.asList(
                                EventsOfTheDay(firstDateOfTimetable!!),
                                EventsOfTheDay(DateUtils.addDays(firstDateOfTimetable, 1)),
                                EventsOfTheDay(DateUtils.addDays(firstDateOfTimetable, 2)),
                                EventsOfTheDay(DateUtils.addDays(firstDateOfTimetable, 3)),
                                EventsOfTheDay(DateUtils.addDays(firstDateOfTimetable, 4)),
                                EventsOfTheDay(DateUtils.addDays(firstDateOfTimetable, 5))
                            )
                        )
                    )
                )
                showTimetable.value!!.second.add(
                    addHeadersInLessons(
                        groupWeekLessonsList!![groupWeekLessonsList!!.size - 1]
                    )
                )
                showAddedGroup.call()
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