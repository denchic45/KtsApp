package com.denchic45.kts.ui.timetable

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.EventsOfDay
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.data.repository.EventRepository
import com.denchic45.kts.data.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import java.time.LocalDate
import javax.inject.Inject

class TimetableInteractor @Inject constructor(
    private val eventRepository: EventRepository,
    private val subjectRepository: SubjectRepository,
    private val groupPreference: GroupPreference,
    private val userPreference: UserPreference
) : Interactor {

    fun findEventsOfGroupByDate(date: LocalDate, groupId: String): Flow<EventsOfDay> {
        return eventRepository.findEventsOfDayByGroupIdAndDate(groupId, date)
    }

    val lessonTime: Int get() = eventRepository.lessonTime

    override fun removeListeners() {
        eventRepository.removeListeners()
        subjectRepository.removeListeners()
    }

    fun observeYourGroupId() =
        groupPreference.observeValue(GroupPreference.GROUP_ID, "").filter(String::isNotEmpty)

    val role: String
        get() = userPreference.role

}