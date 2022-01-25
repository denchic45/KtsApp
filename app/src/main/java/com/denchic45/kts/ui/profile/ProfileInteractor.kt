package com.denchic45.kts.ui.profile

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.data.repository.StudentRepository
import com.denchic45.kts.data.repository.TeacherRepository
import com.denchic45.kts.data.repository.UserRepository
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class ProfileInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
    private val groupInfoRepository: GroupInfoRepository
) : Interactor {

    fun find(id: String): Observable<User> {
        return userRepository.findById(id)
    }

    fun findThisUser(): User {
        return userRepository.findSelf()
    }

    fun findGroupByStudent(user: User): Observable<Group> {
        return groupInfoRepository.findGroupByStudent(user)
    }

    fun findGroupByCurator(user: User): Observable<Group> {
        return groupInfoRepository.findGroupByCurator(user)
    }

    override fun removeListeners() {
        userRepository.removeListeners()
        groupInfoRepository.removeListeners()
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