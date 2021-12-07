package com.denchic45.kts.ui.group.courses

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.Course
import com.denchic45.kts.data.model.domain.CourseInfo
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.CourseRepository
import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupCoursesInteractor @Inject constructor(
    private val courseRepository: CourseRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository
) : Interactor() {

    fun findCoursesByGroupUuid(groupUuid: String): Flow<List<CourseInfo>> {
        return courseRepository.findByGroupUuid(groupUuid)
    }

    override fun removeListeners() {}

   suspend fun removeCourse(course: Course) {
        courseRepository.remove(course)
    }

    val yourGroupUuid: String
        get() = groupRepository.yourGroupUuid

    fun findThisUser(): User {
        return userRepository.findThisUser()
    }
}