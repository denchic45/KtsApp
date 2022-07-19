package com.denchic45.kts.domain.usecase

import android.util.Log
import com.denchic45.kts.domain.model.Section
import com.denchic45.kts.data.repository.CourseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindCourseSectionsUseCase @Inject constructor(private val courseRepository: CourseRepository) {
    operator fun invoke(courseId: String): Flow<List<Section>> {
        Log.d("lol", "sections invoke: ")
        return courseRepository.findSectionsByCourseId(courseId)
    }
}