package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Section
import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class UpdateCourseSectionsUseCase @Inject constructor(private  val courseRepository: CourseRepository) {

    suspend operator fun invoke(sections: List<Section>) = courseRepository.updateCourseSections(sections)
}