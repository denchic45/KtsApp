package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.repository.GroupInfoRepository
import javax.inject.Inject

class UpdateGroupUseCase @Inject constructor(
    private val groupInfoRepository: GroupInfoRepository) {

    suspend operator fun invoke(group: Group) {
        groupInfoRepository.update(group)
    }
}