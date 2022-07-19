package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Course
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindCourseUseCase @Inject constructor(
    private val courserRepository: CourseRepository
) {
    operator fun invoke(params: String): Flow<Course?> {
        return courserRepository.find(params)
    }
}
