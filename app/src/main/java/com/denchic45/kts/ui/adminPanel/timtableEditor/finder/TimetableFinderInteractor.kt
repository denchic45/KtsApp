package com.denchic45.kts.ui.adminPanel.timtableEditor.finder

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource2
import com.denchic45.kts.data.model.domain.Event
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.prefs.AppPreference
import com.denchic45.kts.data.repository.EventRepository
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.utils.DateFormatUtil
import kotlinx.coroutines.flow.Flow
import java.util.*
import javax.inject.Inject

class TimetableFinderInteractor @Inject constructor(
    private val groupInfoRepository: GroupInfoRepository,
    private val eventRepository: EventRepository,
    private val appPreference: AppPreference
) : Interactor {
    fun findGroupByTypedName(groupName: String): Flow<Resource2<List<Group>>> {
        return groupInfoRepository.findByTypedName(groupName)
    }

    override fun removeListeners() {}

    fun findLessonsOfGroupByDate(date: Date, groupUuid: String): Flow<List<Event>> {
        return eventRepository.findLessonsOfGroupByDate(date, groupUuid)
    }

    val lessonTime: Int
        get() = appPreference.lessonTime

    suspend fun updateGroupLessonOfDay(lessons: List<Event>, date: Date, group: Group) {
        eventRepository.updateEventsOfDay(
            lessons,
            DateFormatUtil.convertDateToDateUTC(date),
            group
        )
    }
}