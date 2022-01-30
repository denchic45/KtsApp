package com.denchic45.kts.ui.timetable

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.utils.DateFormatUtil
import com.denchic45.widget.calendar.model.Week
import java.util.*
import java.util.function.Function
import javax.inject.Inject
import javax.inject.Named

class TimetableViewModel @Inject constructor(
    application: Application,
    @Named(TimetableFragment.GROUP_ID) groupId: String?,
    private val interactor: TimetableInteractor
) : AndroidViewModel(application) {
    val showLessonsOfDay: LiveData<List<Event>>
    val title = MutableLiveData<String>()
    val showListState = MutableLiveData<String>()
    private val lessonsDate = MutableLiveData<Date>()
    val initTimetable = MutableLiveData<Boolean>()
    private var groupId: String

    private var findEventsByDate: Function<Date, LiveData<List<Event>>>
    fun onWeekSelect(week: Week) {
        val selectedDay = week.selectedDay
        if (selectedDay == -1) {
            title.setValue(DateFormatUtil.convertDateToStringHidingCurrentYear(week.getDate(3)))
        } else {
            title.setValue(
                DateFormatUtil.convertDateToStringHidingCurrentYear(
                    week.getDate(
                        selectedDay
                    )
                )
            )
        }
    }

    fun onWeekLoad(week: Week) {
        Log.d("lol", "onWeekLoad: $week")
    }

    fun onDaySelect(date: Date) {
        lessonsDate.value = date
        title.value = DateFormatUtil.convertDateToStringHidingCurrentYear(date)
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
            } else if(User.isStudent(role)) {
                throw Exception("Navigation state problem. No group")
            }
        } else {
            this.groupId = groupId
        }

        findEventsByDate = if (User.isStudent(role)) {
            initTimetable.value = false
            Function { date: Date -> interactor.findEventsOfGroupByDate(date, this.groupId) }
        } else {
            initTimetable.value = true
            Function { date -> interactor.findEventsForTeacherByDate(date) }
        }
        showLessonsOfDay = Transformations.switchMap(lessonsDate) { date ->
            val cal = Calendar.getInstance()
            cal.time = date
            if (cal[Calendar.DAY_OF_WEEK] == Calendar.SUNDAY) {
                showListState.value = TimetableFragment.DAY_OFF_VIEW
                return@switchMap MutableLiveData<List<Event>>()
            }
            showListState.value = null
            findEventsByDate.apply(date)
        }
        lessonsDate.value = Date()
        title.value = DateFormatUtil.convertDateToStringHidingCurrentYear(Date())
    }
}