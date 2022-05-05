package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.repository.GroupRepository
import com.denchic45.kts.data.repository.StudentRepository
import javax.inject.Inject

class SetHeadmanUseCase @Inject constructor(private val groupRepository: GroupRepository) {

    suspend operator fun invoke(studentId: String, groupId: String) {
        groupRepository.setHeadman(studentId, groupId)
    }
}