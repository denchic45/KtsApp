package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Task
import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class UpdateSubmissionFromStudentUseCase @Inject constructor(private val courseRepository: CourseRepository) {

    suspend operator fun invoke(submission: Task.Submission) {
        courseRepository.updateSubmissionFromStudent(submission)
    }
}