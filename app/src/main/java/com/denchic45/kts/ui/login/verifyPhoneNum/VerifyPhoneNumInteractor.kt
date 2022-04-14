package com.denchic45.kts.ui.login.verifyPhoneNum

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.repository.*
import kotlinx.coroutines.channels.Channel
import javax.inject.Inject

class VerifyPhoneNumInteractor @Inject constructor(
    private val authRepository: AuthRepository,
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val groupRepository: GroupRepository
) : Interactor {

//    fun sendUserPhoneNumber(phoneNum: String): Channel<String> {
//        return  authRepository.sendUserPhoneNumber(phoneNum)
//    }

//   suspend fun tryAuthWithPhoneNumByCode(code: String) {
//        authRepository.authByPhoneNum(code)
//    }

    override fun removeListeners() {
        groupRepository.removeListeners()
        authRepository.removeListeners()
        eventRepository.removeListeners()
        subjectRepository.removeListeners()
        userRepository.removeListeners()
    }

//    fun resendCode() {
//        authRepository.resendCodeSms()
//    }
}