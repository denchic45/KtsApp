package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class RejectSubmissionUseCase @Inject constructor(private val courseRepository: CourseRepository) {
    suspend operator fun invoke(taskId: String, studentId: String, cause: String) {
        courseRepository.rejectSubmission(taskId, studentId, cause)
    }
}