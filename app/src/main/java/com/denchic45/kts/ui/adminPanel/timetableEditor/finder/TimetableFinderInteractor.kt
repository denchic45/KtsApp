package com.denchic45.kts.ui.adminPanel.timetableEditor.finder

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Event
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
) : Interactor {
    fun findGroupByTypedName(groupName: String): Flow<List<CourseGroup>> {
        return groupRepository.findByTypedName(groupName)
    }

    override fun removeListeners() {}

    fun findLessonsOfGroupByDate(date: LocalDate, groupId: String): Flow<EventsOfDay> {
        return eventRepository.findEventsOfDayByGroupIdAndDate(groupId, date)
    }

    val lessonTime: Int
        get() = appPreference.lessonTime

    suspend fun updateGroupLessonOfDay(lessons: List<Event>, date: LocalDate, group: CourseGroup) {
        eventRepository.updateEventsOfDay(
            lessons,
            date,
            group
        )
    }
}