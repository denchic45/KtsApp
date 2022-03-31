package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindGroupNameUseCase @Inject constructor(
    private val groupRepository: GroupRepository
) {

    operator fun invoke(groupId: String): Flow<String> {
       return groupRepository.getNameByGroupId(groupId)
    }
}