package com.denchic45.kts.ui.group.courses

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.CourseHeader
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupCoursesInteractor @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) : Interactor {

    fun findCoursesByGroupId(groupId: String): Flow<List<CourseHeader>> {
        return courseRepository.findByGroupId(groupId)
    }

    override fun removeListeners() {}

    val yourGroupId: String
        get() = groupRepository.yourGroupId

    fun findThisUser(): User {
        return userRepository.findSelf()
    }
}