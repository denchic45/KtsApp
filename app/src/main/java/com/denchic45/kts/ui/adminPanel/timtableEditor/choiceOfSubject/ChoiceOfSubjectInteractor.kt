package com.denchic45.kts.ui.adminPanel.timtableEditor.choiceOfSubject

import androidx.lifecycle.LiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.repository.SubjectRepository
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
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


    val subjectsOfGroup: LiveData<Resource<List<Subject>>>
        get() = subjectRepository.findByGroup(groupId)

    fun postSelectedSubject(subject: Subject) {
        selectedSubject.onNext(subject)
    }

    fun findSubjectByTypedName(name: String): Flow<Resource<List<Subject>>> {
        return subjectRepository.findByTypedName(name)
    }
}