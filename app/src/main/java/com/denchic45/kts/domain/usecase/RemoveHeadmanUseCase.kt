package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.StudentRepository
import javax.inject.Inject

class RemoveHeadmanUseCase @Inject constructor(private val groupRepository: GroupRepository) {

    suspend operator fun invoke(groupId: String) {
        groupRepository.removeHeadman(groupId)
    }
}