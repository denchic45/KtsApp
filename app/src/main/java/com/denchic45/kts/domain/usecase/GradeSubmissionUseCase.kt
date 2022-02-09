package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class GradeSubmissionUseCase @Inject constructor(private val courseRepository: CourseRepository) {
    suspend operator fun invoke(taskId: String, studentId: String, grade: Int) {
        courseRepository.gradeSubmission(taskId, studentId, grade)
    }
}