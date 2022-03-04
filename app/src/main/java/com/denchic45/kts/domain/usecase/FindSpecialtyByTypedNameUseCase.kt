package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.SpecialtyRepository
import com.denchic45.kts.data.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindSpecialtyByTypedNameUseCase @Inject constructor(private val specialtyRepository: SpecialtyRepository) {
    operator fun invoke(name: String): Flow<List<Specialty>> {
        return specialtyRepository.findByTypedName(name)
    }
}