package com.denchic45.kts.ui.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.GroupInfoRepository
import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupInteractor @Inject constructor(
    private val groupRepository: GroupRepository,
    private val groupInfoRepository: GroupInfoRepository,
    private val userRepository: UserRepository
) : Interactor {

    val yourGroupUuid: String
        get() = groupRepository.yourGroupId

    override fun removeListeners() {}
    val yourGroupName: String
        get() = groupRepository.yourGroupName

    fun getNameByGroupUuid(groupUuid: String): Flow<String> {
        return groupInfoRepository.getNameByGroupUuid(groupUuid)
    }

    fun isExistGroup(groupUuid: String): LiveData<Boolean> {
        return groupInfoRepository.isExistGroup(groupUuid).asLiveData()
    }

    fun findThisUser(): User {
        return userRepository.findSelf()
    }
}