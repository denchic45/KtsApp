package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.GroupHeader
import com.denchic45.kts.data.repository.GroupRepository
import javax.inject.Inject

class FindGroupByContainsNameUseCase @Inject constructor(
    groupRepository: GroupRepository
) : FindByContainsNameUseCase<GroupHeader>(groupRepository)