package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindTeacherByTypedNameUseCase @Inject constructor(private val teacherRepository: TeacherRepository) {
    operator fun invoke(name: String): Flow<List<User>> {
        return teacherRepository.findByTypedName(name)
    }
}