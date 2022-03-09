package com.denchic45.kts.ui.timetable

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.EventsOfDay
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.data.repository.EventRepository
import com.denchic45.kts.data.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class TimetableInteractor @Inject constructor(
    private val eventRepository: EventRepository,
    private val subjectRepository: SubjectRepository,
    private val groupPreference: GroupPreference,
    private val userPreference: UserPreference
) : Interactor {

    fun findEventsOfGroupByDate(date: LocalDate, groupId: String): Flow<EventsOfDay> {
        return eventRepository.findLessonOfYourGroupByDate(date, groupId)
    }

    fun findEventsForTeacherByDate(date: LocalDate): Flow<EventsOfDay> {
        return eventRepository.findLessonsForTeacherByDate(date)
    }

    fun hasGroup() = groupPreference.groupId.isNotEmpty()

    val lessonTime: Int get() = eventRepository.lessonTime

    override fun removeListeners() {
        eventRepository.removeListeners()
        subjectRepository.removeListeners()
    }

    fun yourGroupId(): String = groupPreference.groupId


    val role: String
        get() = userPreference.role

}