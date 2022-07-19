package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.domain.model.Section
import javax.inject.Inject

class AddCourseSectionsUseCase @Inject constructor(private val courseRepository: CourseRepository) {
    suspend operator fun invoke(section: Section) {
        courseRepository.addSection(section)
    }
}