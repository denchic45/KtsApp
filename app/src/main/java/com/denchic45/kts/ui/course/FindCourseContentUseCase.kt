package com.denchic45.kts.ui.course

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindCourseContentUseCase @Inject constructor(private val courseRepository: CourseRepository) {
    operator fun invoke(courseId: String): Flow<List<DomainModel>> {
        return courseRepository.findContentByCourseId(courseId)
    }

}