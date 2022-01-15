package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindCourseContentUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    operator fun invoke(id: String): Flow<Task> = courseRepository.findTask(id)

}