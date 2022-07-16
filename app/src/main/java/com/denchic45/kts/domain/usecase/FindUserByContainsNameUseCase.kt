package com.denchic45.kts.domain.usecase

import com.denchic45.kts.domain.model.User
import com.denchic45.kts.data.repository.UserRepository
import javax.inject.Inject

class FindUserByContainsNameUseCase @Inject constructor(
    userRepository: UserRepository
) : FindByContainsNameUseCase<User>(userRepository)