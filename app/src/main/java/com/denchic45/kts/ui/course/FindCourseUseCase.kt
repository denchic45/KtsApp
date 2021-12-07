package com.denchic45.kts.ui.course

import com.denchic45.kts.data.model.domain.Course
import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.usecase.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindCourseUseCase @Inject constructor(
   private val courserRepository: CourseRepository
): FlowUseCase<Course, String>() {
    override fun invoke(params: String): Flow<Course> {
       return courserRepository.find(params)
    }
}
