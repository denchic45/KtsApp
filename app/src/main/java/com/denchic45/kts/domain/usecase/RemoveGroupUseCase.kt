package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.repository.GroupRepository
import javax.inject.Inject

class RemoveGroupUseCase @Inject constructor(
    private val courseRepository: CourseRepository,
    private val groupRepository: GroupRepository
) {

    suspend operator fun invoke(groupId: String) {
        courseRepository.removeGroupFromCourses(groupId)
        groupRepository.remove(groupId)
    }
}