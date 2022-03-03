package com.denchic45.kts.ui.timetable

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.Dates
import com.denchic45.kts.utils.capitalized
import com.denchic45.kts.utils.toDate
import com.denchic45.widget.calendar.model.WeekItem
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.util.*
import java.util.function.Function
import javax.inject.Inject
import javax.inject.Named

class TimetableViewModel @Inject constructor(
    @Named(TimetableFragment.GROUP_ID) groupId: String?,
    private val interactor: TimetableInteractor
) : BaseViewModel() {
    val showLessonsOfDay: LiveData<List<Event>>
    val showListState = MutableLiveData<String?>()
    private val lessonsDate = MutableLiveData<LocalDate>()
    val initTimetable = MutableLiveData<Boolean>()
    var selectedDate = MutableStateFlow(LocalDate.now())

    private var groupId: String

    private var findEventsByDate: Function<LocalDate, LiveData<List<Event>>>
    fun onWeekSelect(weekItem: WeekItem) {
        val selectedDay = weekItem.selectedDay
        toolbarTitle = if (selectedDay == -1) {
            Dates.toStringHidingCurrentYear(weekItem[3]).capitalized()
        } else {
            Dates.toStringHidingCurrentYear(weekItem[selectedDay]).capitalized()
        }
    }

    fun onWeekLoad(weekItem: WeekItem) {
        //Nothing
    }

    fun onDaySelect(date: LocalDate) {
        lessonsDate.value = date
        toolbarTitle = Dates.toStringHidingCurrentYear(date)
    }

    val lessonTime: Int
        get() = interactor.lessonTime

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    init {
        val role = interactor.role
        if (groupId == null) {
            this.groupId = interactor.yourGroupId()
            if (interactor.hasGroup()) {
                this.groupId = interactor.yourGroupId()
            } else if (User.isStudent(role)) {
                throw Exception("Navigation state problem. No group")
            }
        } else {
            this.groupId = groupId
        }

        findEventsByDate = if (User.isStudent(role)) {
            initTimetable.value = false
            Function { date -> interactor.findEventsOfGroupByDate(date, this.groupId) }
        } else {
            initTimetable.value = true
            Function { date -> interactor.findEventsForTeacherByDate(date) }
        }
        showLessonsOfDay = Transformations.switchMap(lessonsDate) { date ->
            val cal = Calendar.getInstance()
            cal.time = date.toDate()
            if (cal[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
                showListState.value = TimetableFragment.DAY_OFF_VIEW
                return@switchMap MutableLiveData<List<Event>>()
            }
            showListState.value = null
            findEventsByDate.apply(date)
        }
        lessonsDate.value = LocalDate.now()
        toolbarTitle = Dates.toStringHidingCurrentYear(LocalDate.now()).capitalized()

    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_select_today -> {
                selectedDate.value = LocalDate.now()
            }
        }
    }
}