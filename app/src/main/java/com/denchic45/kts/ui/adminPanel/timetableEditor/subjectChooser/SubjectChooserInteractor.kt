package com.denchic45.kts.ui.adminPanel.timetableEditor.subjectChooser

import com.denchic45.kts.data.Resource
import com.denchic45.kts.domain.model.Subject
import com.denchic45.kts.data.repository.SubjectRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectChooserInteractor @Inject constructor(
    private val subjectRepository: SubjectRepository,
) {
    var groupName: String = ""
    var groupId: String = ""

    private val selectedSubject = Channel<Subject>()

    suspend fun receiveSelectedSubject(): Subject {
        return selectedSubject.receive()
    }


    fun subjectsOfGroup(): Flow<Resource<List<Subject>>> = flow {
        try {
            emitAll(subjectRepository.findByGroup(groupId).mapLatest { Resource.Success(it) })
        } catch (e: Exception) {
            emit(Resource.Error(e))
        }
    }


    suspend fun postSelectedSubject(subject: Subject) {
        selectedSubject.send(subject)
    }
}