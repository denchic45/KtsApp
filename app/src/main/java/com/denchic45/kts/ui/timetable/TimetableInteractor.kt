package com.denchic45.kts.ui.timetable

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.data.repository.EventRepository
import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.utils.Events.addMissingEmptyEvents
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class TimetableInteractor @Inject constructor(
    private val eventRepository: EventRepository,
    private val subjectRepository: SubjectRepository,
    private val groupPreference: GroupPreference,
    private val userPreference: UserPreference
) : Interactor {

    fun findEventsOfYourGroupByDate(date: LocalDate): LiveData<List<Event>> {
        return eventRepository.findLessonOfYourGroupByDate(date, groupPreference.groupId)
            .asLiveData()
    }

    fun findEventsOfGroupByDate(date: LocalDate, groupId: String): Flow<List<Event>> {
        return eventRepository.findLessonOfYourGroupByDate(date, groupId)
    }

    fun findEventsForTeacherByDate(date: LocalDate): Flow<List<Event>> {
        return eventRepository.findLessonsForTeacherByDate(date)
            .map { addMissingEmptyEvents(it.toMutableList()) }
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