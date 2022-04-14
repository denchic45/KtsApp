package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.StudentRepository
import javax.inject.Inject

class RemoveStudentUseCase @Inject constructor(private val studentRepository: StudentRepository) {

    suspend operator fun invoke(studentId: String) {
        studentRepository.remove(studentId)
    }
}