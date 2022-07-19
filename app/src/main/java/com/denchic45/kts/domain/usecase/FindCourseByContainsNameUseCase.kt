package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.CourseHeader
import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class FindCourseByContainsNameUseCase @Inject constructor(
    courseRepository: CourseRepository
) : FindByContainsNameUseCase<CourseHeader>(courseRepository)