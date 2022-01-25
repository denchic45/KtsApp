package com.denchic45.kts.ui.adminPanel.timtableEditor.loader

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.TimetableParser
import com.denchic45.kts.data.model.domain.GroupWeekLessons
import com.denchic45.kts.data.repository.EventRepository
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.rx.AsyncTransformer
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.io.File
import javax.inject.Inject

class TimetableLoaderInteractor @Inject constructor(
    private val eventRepository: EventRepository,
    private val groupInfoRepository: GroupInfoRepository,
    private val subjectRepository: SubjectRepository
) : Interactor {

    private lateinit var listPublishSubject: PublishSubject<List<GroupWeekLessons>>
    fun parseDocumentTimetable(docFile: File): Observable<List<GroupWeekLessons>> {
        listPublishSubject = PublishSubject.create()
        TimetableParser(subjectRepository.findSpecialSubjects()).parseDoc(docFile) { course: Int ->
            groupInfoRepository.findGroupsWithCoursesByCourse(course)
        }
            .compose(AsyncTransformer())
            .subscribe { groupWeekLessons: List<GroupWeekLessons>, throwable: Throwable? ->
                if (throwable != null) {
                    throwable.printStackTrace()
                    listPublishSubject.onError(throwable)
                } else {
                    listPublishSubject.onNext(groupWeekLessons)
                }
            }
        return listPublishSubject
    }

    override fun removeListeners() {
        groupInfoRepository.removeListeners()
        eventRepository.removeListeners()
        subjectRepository.removeListeners()
    }

    fun addLessonsOfWeek(groupWeekLessons: List<GroupWeekLessons>): Completable {
        return eventRepository.addLessonsOfWeekForGroups(groupWeekLessons)
    }
}