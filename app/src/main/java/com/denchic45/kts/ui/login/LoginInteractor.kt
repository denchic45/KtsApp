package com.denchic45.kts.ui.login

import android.util.Log
import com.denchic45.kts.data.repository.AuthRepository
import com.denchic45.kts.data.repository.UserRepository
import javax.inject.Inject

class LoginInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {

    suspend fun authByEmail(mail: String, password: String) {
        Log.d("lol", "A authByEmail: ")
        authRepository.authByEmail(mail, password)
        Log.d("lol", "A findAndSaveByEmail: ")
        userRepository.findAndSaveByEmail(mail)
    }
}