package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Task
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindTaskSubmissionsUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {

      operator fun invoke(taskId: String): Flow<List<Task.Submission>> {
        return courseRepository.findTaskSubmissions(taskId)
    }
}