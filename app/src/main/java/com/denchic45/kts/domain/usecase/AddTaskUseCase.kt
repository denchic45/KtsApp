package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.repository.CourseRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val courseRepository: CourseRepository
) {
    suspend operator fun invoke(task: Task){
        courseRepository.addTask(task)
    }

}