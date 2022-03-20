package com.denchic45.kts.ui.timetable

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.Dates
import com.denchic45.kts.utils.capitalized
import com.denchic45.widget.calendar.model.WeekItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Named

class TimetableViewModel @Inject constructor(
    @Named(TimetableFragment.GROUP_ID) receivedGroupId: String?,
    private val interactor: TimetableInteractor
) : BaseViewModel() {
    val initTimetable = MutableSharedFlow<Boolean>(replay = 1)
    val events: StateFlow<EventsState>
    private val groupIdState: Flow<String>
    var selectedDate = MutableSharedFlow<LocalDate>(replay = 1)

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
        viewModelScope.launch {
            selectedDate.emit(date)
            toolbarTitle = Dates.toStringHidingCurrentYear(date).capitalized()
        }
    }

    val lessonTime: Int
        get() = interactor.lessonTime

    override fun onCleared() {
        super.onCleared()
        interactor.removeListeners()
    }

    init {
        val role = interactor.role
        groupIdState = if (receivedGroupId == null) {
            interactor.observeYourGroupId().shareIn(viewModelScope, SharingStarted.Lazily)
        } else {
            flowOf(receivedGroupId)
        }

        viewModelScope.launch { initTimetable.emit(User.isTeacher(role)) }

        events = selectedDate.distinctUntilChanged()
            .combine(groupIdState) { selectedDate, groupId -> selectedDate to groupId }
            .flatMapLatest { (selectedDate, groupId) ->
                Log.d("lol", "events groupId: $groupId selectedDate: $selectedDate")
                if (selectedDate.dayOfWeek == DayOfWeek.SUNDAY)
                    return@flatMapLatest flowOf(EventsState.DayOff)
                if (User.isStudent(role)) {
                    interactor.findEventsOfGroupByDate(selectedDate, groupId)
                        .map { eventsOfDay -> EventsState.Events(eventsOfDay.events) }
                } else {
                    interactor.findEventsForTeacherByDate(selectedDate)
                        .map { eventsOfDay -> EventsState.Events(eventsOfDay.events) }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, EventsState.Events(emptyList()))

        viewModelScope.launch {
            selectedDate.emit(LocalDate.now())
        }
        toolbarTitle = Dates.toStringHidingCurrentYear(LocalDate.now()).capitalized()
    }

    sealed class EventsState {
        data class Events(
            val events: List<Event>
        ) : EventsState()

        object DayOff : EventsState()
    }

    override fun onOptionClick(itemId: Int) {
        when (itemId) {
            R.id.option_select_today -> {
                viewModelScope.launch {
                    selectedDate.emit(LocalDate.now())
                }
            }
        }
    }
}