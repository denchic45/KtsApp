package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import javax.inject.Inject

class FindTeacherByTypedNameUseCase @Inject constructor(private val teacherRepository: TeacherRepository) {
    operator fun invoke(name: String): Flow<Resource<List<User>>> = flow {
        try {
            emitAll(teacherRepository.findByTypedName(name).mapLatest { Resource.Success(it) })
        } catch (t: Throwable) {
            emit(Resource.Error(t))
        }
    }
}