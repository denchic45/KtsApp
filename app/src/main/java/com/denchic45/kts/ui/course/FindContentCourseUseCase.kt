package com.denchic45.kts.ui.course

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.usecase.base.FlowUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindContentCourseUseCase @Inject constructor (private val courseRepository: CourseRepository):
    FlowUseCase<List<DomainModel>, String>() {
    override fun invoke(params: String): Flow<List<DomainModel>> {
       return courseRepository.findContentByCourseUuid(params)
    }

}