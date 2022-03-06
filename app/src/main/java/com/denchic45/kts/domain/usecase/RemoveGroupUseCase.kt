package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.repository.GroupRepository
import javax.inject.Inject

class RemoveGroupUseCase @Inject constructor(
    private val groupRepository: GroupRepository) {

    suspend operator fun invoke(group: Group) {
        groupRepository.remove(group)
    }
}