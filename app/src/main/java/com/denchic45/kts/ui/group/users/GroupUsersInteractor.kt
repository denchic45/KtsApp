package com.denchic45.kts.ui.group.users

import androidx.lifecycle.LiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.data.repository.StudentRepository
import com.denchic45.kts.data.repository.UserRepository
import io.reactivex.rxjava3.core.Completable
import javax.inject.Inject

class GroupUsersInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val groupInfoRepository: GroupInfoRepository,
    private val studentRepository: StudentRepository
) : Interactor {

    val yourGroupId: String
        get() = groupInfoRepository.yourGroupId

    fun getUsersByGroupId(groupId: String): LiveData<List<User>> {
        return userRepository.getByGroupId(groupId)
    }

    fun removeStudent(student: User): Completable {
        return studentRepository.remove(student)
    }

    suspend fun updateGroupCurator(groupId: String, teacherId: User) {
        groupInfoRepository.updateGroupCurator(groupId, teacherId)
    }

    override fun removeListeners() {
        studentRepository.removeListeners()
        userRepository.removeListeners()
        groupInfoRepository.removeListeners()
    }

    fun getCurator(groupId: String): LiveData<User> {
        return groupInfoRepository.findCurator(groupId)
    }

    fun findThisUser(): User {
        return userRepository.findSelf()
    }
}