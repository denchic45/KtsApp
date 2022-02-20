package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindCourseContentUseCase @Inject constructor(private val courseRepository: CourseRepository):Interactor {
    operator fun invoke(courseId: String): Flow<List<DomainModel>> {
        return courseRepository.findContentByCourseId(courseId)
    }

    override fun removeListeners() {
        courseRepository.removeListeners()
    }

}