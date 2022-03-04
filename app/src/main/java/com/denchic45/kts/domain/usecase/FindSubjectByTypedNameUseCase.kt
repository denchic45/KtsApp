package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.repository.SubjectRepository
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FindSubjectByTypedNameUseCase @Inject constructor(private val subjectRepository: SubjectRepository) {
    operator fun invoke(name: String): Flow<Resource<List<Subject>>> {
        return try {
            subjectRepository.findByTypedName(name)
                .map { Resource.Success(it) }
        } catch (e: NetworkException) {
            flowOf(Resource.Error(e))
        }
    }
}