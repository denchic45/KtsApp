package com.denchic45.kts.ui.group.users

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.StudentRepository
import com.denchic45.kts.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupUsersInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val studentRepository: StudentRepository
) : Interactor {

    val yourGroupId: String
        get() = groupRepository.yourGroupId

    fun getUsersByGroupId(groupId: String): Flow<List<User>> {
        return userRepository.findByGroupId(groupId)
    }

    suspend fun removeStudent(student: User) {
        studentRepository.remove(student)
    }

    suspend fun updateGroupCurator(groupId: String, teacherId: User) {
        groupRepository.updateGroupCurator(groupId, teacherId)
    }

    override fun removeListeners() {
        studentRepository.removeListeners()
        userRepository.removeListeners()
        groupRepository.removeListeners()
    }

    fun getCurator(groupId: String): Flow<User> {
        return groupRepository.findCurator(groupId)
    }

    fun findThisUser(): User {
        return userRepository.findSelf()
    }
}