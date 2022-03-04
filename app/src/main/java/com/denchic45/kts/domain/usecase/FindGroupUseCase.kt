package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.repository.GroupInfoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FindGroupUseCase @Inject constructor(
    private val groupInfoRepository: GroupInfoRepository
) {

    operator fun invoke(groupId: String): Flow<Group> {
       return groupInfoRepository.find(groupId)
    }
}