package com.denchic45.kts.ui.login

import com.denchic45.kts.data.repository.AuthRepository
import com.denchic45.kts.data.repository.UserRepository
import javax.inject.Inject

class LoginInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {

    suspend fun findUserByPhoneNum(phoneNum: String) {
        userRepository.findAndSaveByPhoneNum(phoneNum)
    }

    suspend fun authByEmail(mail: String, password: String) {
        authRepository.authByEmail(mail, password)
        userRepository.findAndSaveByEmail(mail)
    }
}