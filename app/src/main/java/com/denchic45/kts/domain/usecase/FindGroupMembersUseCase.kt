package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.GroupMembers
import com.denchic45.kts.data.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindGroupMembersUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {

    operator fun invoke(groupId: String): Flow<GroupMembers> {
        return groupRepository.findGroupMembersByGroupId(groupId)
    }
}