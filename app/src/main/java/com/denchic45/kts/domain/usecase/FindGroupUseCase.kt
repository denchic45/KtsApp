package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.Group
import com.denchic45.kts.data.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {

    operator fun invoke(groupId: String): Flow<Group?> {
       return groupRepository.find(groupId)
    }
}