package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import com.denchic45.kts.data.model.domain.GroupHeader
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

    fun findEventsOfDayByGroup(date: LocalDate, groupId: String): Flow<EventsOfDay> {
        return eventRepository.findEventsOfDayByGroupIdAndDate(groupId, date)
    }

    val lessonTime: Int
        get() = appPreference.lessonTime

    suspend fun updateGroupEventsOfDay(eventsOfDay: EventsOfDay, groupHeader: GroupHeader) {
        eventRepository.updateEventsOfDay(eventsOfDay, groupHeader)
    }
}