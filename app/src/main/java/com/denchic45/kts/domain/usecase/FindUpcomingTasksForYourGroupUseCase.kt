package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Task
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindUpcomingTasksForYourGroupUseCase @Inject constructor(
   private val courseRepository: CourseRepository
) {

    operator fun invoke(): Flow<List<Task>> {
        return courseRepository.findUpcomingTasksForYourGroup()
    }
}