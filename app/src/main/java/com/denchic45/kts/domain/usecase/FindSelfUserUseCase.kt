package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.UserRepository
import com.denchic45.kts.domain.usecase.base.SyncUseCase
import javax.inject.Inject

class FindSelfUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) : SyncUseCase<User, Nothing>() {
    override fun invoke(params: Nothing?): User = userRepository.findSelf()

}