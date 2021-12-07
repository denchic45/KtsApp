package com.denchic45.kts.ui.login

import com.denchic45.kts.data.repository.AuthRepository
import com.denchic45.kts.data.repository.UserRepository
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.CompletableEmitter
import javax.inject.Inject

class LoginInteractor @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) {

    fun findUserByPhoneNum(phoneNum: String): Completable {
        return userRepository.findByPhoneNum(phoneNum)
    }

    fun authByEmail(mail: String, password: String): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            authRepository.authByEmail(mail, password)
                .subscribe({
                    userRepository.findByEmail(mail)
                        .subscribe({ emitter.onComplete() }, emitter::onError)
                }, emitter::onError)
        }
    }
}