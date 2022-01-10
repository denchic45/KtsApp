package com.denchic45.kts.domain

import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(task: Task){
        courseRepository.updateTask(task)
    }

}