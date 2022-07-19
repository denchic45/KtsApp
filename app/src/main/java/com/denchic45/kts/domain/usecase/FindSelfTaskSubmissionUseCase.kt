package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Task
import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindSelfTaskSubmissionUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository
) {
    operator fun invoke(taskId: String): Flow<Task.Submission> =
        courseRepository.findTaskSubmissionByContentIdAndStudentId(
            taskId,
            userRepository.findSelf().id
        )

}