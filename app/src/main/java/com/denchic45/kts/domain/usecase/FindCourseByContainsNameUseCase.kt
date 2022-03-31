package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.CourseHeader
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.repository.TeacherRepository
import com.denchic45.kts.data.repository.UserRepository
import javax.inject.Inject

class FindCourseByContainsNameUseCase @Inject constructor(
    courseRepository: CourseRepository
) : FindByContainsNameUseCase<CourseHeader>(courseRepository)