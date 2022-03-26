package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.EventsOfDay
import com.denchic45.kts.data.prefs.AppPreference
import com.denchic45.kts.data.repository.EventRepository
import com.denchic45.kts.data.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class TimetableFinderInteractor @Inject constructor(
    private val groupRepository: GroupRepository,
    private val eventRepository: EventRepository,
    private val appPreference: AppPreference
) {
    fun findGroupByTypedName(groupName: String): Flow<List<CourseGroup>> {
        return groupRepository.findByTypedName(groupName)
    }

    fun findLessonsOfGroupByDate(date: LocalDate, groupId: String): Flow<EventsOfDay> {
        return eventRepository.findEventsOfDayByGroupIdAndDate(groupId, date)
    }

    val lessonTime: Int
        get() = appPreference.lessonTime

    suspend fun updateGroupEventsOfDay(eventsOfDay: EventsOfDay, group: CourseGroup) {
        eventRepository.updateEventsOfDay(eventsOfDay, group)
    }
}