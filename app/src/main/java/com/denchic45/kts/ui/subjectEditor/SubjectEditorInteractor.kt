package com.denchic45.kts.ui.subjectEditor

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubjectEditorInteractor @Inject constructor(
    private val subjectRepository: SubjectRepository
) : Interactor {

    suspend fun add(subject: Subject) {
        return subjectRepository.add(subject)
    }

    suspend fun update(subject: Subject) {
        return subjectRepository.update(subject)
    }

    suspend fun remove(subject: Subject) {
        return subjectRepository.remove(subject)
    }


    fun find(id: String): Flow<Subject?> {
        return subjectRepository.find(id)
    }

    override fun removeListeners() {}
}