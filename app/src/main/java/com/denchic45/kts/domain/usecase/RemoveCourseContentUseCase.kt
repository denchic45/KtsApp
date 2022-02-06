package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.CourseContent
import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class RemoveCourseContentUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(taskId: String) = courseRepository.removeCourseContent(taskId)

}