package com.denchic45.kts.ui.profile

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.StudentRepository
import com.denchic45.kts.data.repository.TeacherRepository
import com.denchic45.kts.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProfileInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
    private val groupRepository: GroupRepository
) : Interactor {

    fun observe(id: String): Flow<User?> {
        return userRepository.observeById(id)
    }

    fun findThisUser(): User {
        return userRepository.findSelf()
    }

    fun findGroupByStudent(user: User): Flow<Group> {
        return groupRepository.findGroupByStudent(user)
    }

    fun findGroupByCurator(user: User): Flow<Group> {
        return groupRepository.findGroupByCurator(user)
    }

    override fun removeListeners() {
        userRepository.removeListeners()
        groupRepository.removeListeners()
    }

    suspend fun updateAvatar(user: User, imageBytes: ByteArray) {
        val photoUrl = userRepository.loadAvatar(imageBytes, user.id)
        val updatedUser = user.copy(photoUrl = photoUrl, generatedAvatar = false)
        when {
            updatedUser.isStudent -> {
                studentRepository.update(updatedUser)
            }
            updatedUser.isTeacher -> {
                teacherRepository.update(updatedUser)
            }
        }
    }
}