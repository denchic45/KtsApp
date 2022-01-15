package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Section
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindCourseSectionsUseCase @Inject constructor(private val courseRepository: CourseRepository) {
    operator fun invoke(courseId: String): Flow<List<Section>> {
        return courseRepository.findSectionsByCourseId(courseId)
    }
}