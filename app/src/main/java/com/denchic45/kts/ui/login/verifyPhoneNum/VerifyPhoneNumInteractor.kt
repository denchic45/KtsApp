package com.denchic45.kts.ui.login.verifyPhoneNum

import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.Resource2
import com.denchic45.kts.data.repository.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.NonNull
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class VerifyPhoneNumInteractor @Inject constructor(
    private val authRepository: AuthRepository,
    private val groupRepository: GroupRepository,
    private val subjectRepository: SubjectRepository,
    private val userRepository: UserRepository,
    private val eventRepository: EventRepository,
    private val groupInfoRepository: GroupInfoRepository
) : Interactor {

    fun sendUserPhoneNumber(phoneNum: String): Observable<Resource2<String>> {
        return Observable.create { emitter: ObservableEmitter<Resource2<String>> ->
            authRepository.sendUserPhoneNumber(phoneNum).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ value ->
                    emitter.onNext((value as @NonNull Resource2.Success))
                }, { error: Throwable -> emitter.onError(error) })
        }
    }

    fun tryAuthWithPhoneNumByCode(code: String?) {
        authRepository.authByPhoneNum(code)
    }

    override fun removeListeners() {
        groupInfoRepository.removeListeners()
        groupRepository.removeListeners()
        authRepository.removeListeners()
        eventRepository.removeListeners()
        subjectRepository.removeListeners()
        userRepository.removeListeners()
    }

    fun resendCode() {
        authRepository.resendCodeSms()
    }
}