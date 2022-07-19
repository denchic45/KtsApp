package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Section
import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class FindSectionUseCase @Inject constructor(private val courseRepository: CourseRepository) {

    suspend operator fun invoke(sectionId: String): Section? {
       return courseRepository.findSection(sectionId)
    }
}