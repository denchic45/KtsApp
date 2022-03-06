package com.denchic45.kts.ui.adminPanel.timetableEditor.choiceOfSubject

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.repository.SubjectRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class ChoiceOfSubjectInteractor @Inject constructor(
    private val subjectRepository: SubjectRepository,
) : Interactor {

    var groupName: String = ""
    var groupId: String = ""

    private val selectedSubject = PublishSubject.create<Subject>()

    override fun removeListeners() {}
    fun observeSelectedSubject(): Observable<Subject> {
        return selectedSubject
    }


    fun subjectsOfGroup(): Flow<Resource<List<Subject>>> = flow {
        try {
            emitAll(subjectRepository.findByGroup(groupId).mapLatest { Resource.Success(it) })
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }


    fun postSelectedSubject(subject: Subject) {
        selectedSubject.onNext(subject)
    }

    fun findSubjectByTypedName(name: String): Flow<List<Subject>> {
        return subjectRepository.findByTypedName(name)
    }
}