package com.denchic45.kts.ui.adminPanel.timetableEditor.loader

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.TimetableParser
import com.denchic45.kts.data.model.domain.GroupTimetable
import com.denchic45.kts.data.repository.EventRepository
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.data.repository.SubjectRepository
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject

class TimetableLoaderInteractor @Inject constructor(
    private val eventRepository: EventRepository,
    private val groupInfoRepository: GroupInfoRepository,
    private val subjectRepository: SubjectRepository
) : Interactor {

    suspend fun parseDocumentTimetable(docFile: File): List<GroupTimetable> {
        return TimetableParser(subjectRepository.findSpecialSubjects()).parseDoc(docFile) { course: Int ->
            groupInfoRepository.findGroupsWithCoursesByCourse(course)
        }
    }

    override fun removeListeners() {
        groupInfoRepository.removeListeners()
        eventRepository.removeListeners()
        subjectRepository.removeListeners()
    }

   suspend fun addTimetables(groupWeekLessons: List<GroupTimetable>) {
//       delay(1500)
       //TODO РАСКОММЕНТИРОВАТЬ!!!
       eventRepository.addLessonsOfWeekForGroups(groupWeekLessons)
    }
}