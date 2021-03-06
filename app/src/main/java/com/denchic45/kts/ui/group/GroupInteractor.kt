package com.denchic45.kts.ui.group

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupInteractor @Inject constructor(
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository
) : Interactor {

    val yourGroupId: String
        get() = groupRepository.yourGroupId

    override fun removeListeners() {}

    val yourGroupName: String
        get() = groupRepository.yourGroupName

    fun getNameByGroupId(groupId: String): Flow<String> {
        return groupRepository.getNameByGroupId(groupId)
    }

    fun isExistGroup(groupId: String): Flow<Boolean> {
        return groupRepository.isExistGroup(groupId)
    }

    fun findThisUser(): User {
        return userRepository.findSelf()
    }
}