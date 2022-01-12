package com.denchic45.kts.ui.timetable

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asLiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.data.repository.EventRepository
import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.utils.DateFormatUtil
import com.denchic45.kts.utils.Events.addMissingEmptyEvents
import java.util.*
import javax.inject.Inject

class TimetableInteractor @Inject constructor(
    private val eventRepository: EventRepository,
    private val subjectRepository: SubjectRepository,
    private val groupPreference: GroupPreference,
    private val userPreference: UserPreference
) : Interactor {

    fun findEventsOfYourGroupByDate(date: Date): LiveData<List<Event>> {
        return eventRepository.findLessonOfYourGroupByDate(date, groupPreference.groupUuid)
            .asLiveData()
    }

    fun findEventsOfGroupByDate(date: Date, groupUuid: String): LiveData<List<Event>> {
        return eventRepository.findLessonOfYourGroupByDate(date, groupUuid).asLiveData()
    }

    fun findEventsForTeacherByDate(date: Date): LiveData<List<Event>> {
        return Transformations.map(
            eventRepository.findLessonsForTeacherByDate(DateFormatUtil.convertDateToDateUTC(date))
                .asLiveData()
        ) { addMissingEmptyEvents(it.toMutableList()) }
    }

    fun updateHomeworkCompletion(checked: Boolean) {
        eventRepository.updateHomeworkCompletion(checked)
    }

    fun hasGroup() = groupPreference.groupUuid.isNotEmpty()

    val lessonTime: Int get() = eventRepository.lessonTime

    override fun removeListeners() {
        eventRepository.removeListeners()
        subjectRepository.removeListeners()
    }

    fun yourGroupUuid(): String = groupPreference.groupUuid


    val role: String
        get() = userPreference.role

}