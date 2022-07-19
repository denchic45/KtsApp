package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class UpdateCourseContentOrderUseCase @Inject constructor(private val courseRepository: CourseRepository) {

    suspend operator fun invoke(contentId: String, order: Int) {
        courseRepository.updateContentOrder(contentId, order)
    }

}