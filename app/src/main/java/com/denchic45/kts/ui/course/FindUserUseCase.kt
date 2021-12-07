package com.denchic45.kts.ui.course

import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.UserRepository
import com.denchic45.kts.data.usecase.SyncUseCase
import javax.inject.Inject

class FindUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) :
    SyncUseCase<User, Nothing>() {
    override operator fun invoke(params: Nothing?): User {
        return userRepository.findThisUser()
    }
}
