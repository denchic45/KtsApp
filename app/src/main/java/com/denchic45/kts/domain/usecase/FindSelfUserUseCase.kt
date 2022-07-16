package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.User
import com.denchic45.kts.data.repository.UserRepository
import javax.inject.Inject

class FindSelfUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): User = userRepository.findSelf()
}