package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindTaskSubmissionUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    operator fun invoke(taskId: String, studentId: String): Flow<Task.Submission> {
        return courseRepository.findTaskSubmissionByContentIdAndStudentId(taskId, studentId)
    }

}