package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class IsCourseTeacherUseCase @Inject constructor(private val courseRepository: CourseRepository) {

    suspend operator fun invoke(userId:String, courseId: String): Boolean {
      return  courseRepository.isCourseTeacher(userId, courseId)
    }
}