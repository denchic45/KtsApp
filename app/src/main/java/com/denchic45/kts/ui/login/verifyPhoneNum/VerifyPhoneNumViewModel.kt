package com.denchic45.kts.ui.login.verifyPhoneNum

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.Resource
import com.denchic45.kts.ui.base.BaseViewModel
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class VerifyPhoneNumViewModel @Inject constructor(
    private val interactor: VerifyPhoneNumInteractor
) : BaseViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val btnAuthVisibility = SingleLiveData(false)

    val authSuccessful = SingleLiveData<String>()

    val errorToManyRequest: SingleLiveData<*> = SingleLiveData<Any>()

    val errorInvalidRequest: SingleLiveData<*> = SingleLiveData<Any>()

    val showProgressTimeOut = MutableLiveData<Int>()

    val enableResendCode = MutableLiveData<Boolean>()
    fun onVerifyClick(phoneNum: String) {
        val subscribe = interactor.sendUserPhoneNumber(phoneNum).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ event ->
                when (event) {
                    is Resource.Success -> {
                        authSuccessful.value = event.data
                    }
                    is Resource.Error -> {
                        if (event.error is FirebaseTooManyRequestsException) {
                            errorToManyRequest.call()
                        } else if (event.error is FirebaseAuthInvalidCredentialsException) {
                            errorInvalidRequest.call()
                        }
                    }

                }
            }, { throwable: Throwable ->
                if (throwable is FirebaseTooManyRequestsException) {
                    errorToManyRequest.call()
                } else if (throwable is FirebaseAuthInvalidCredentialsException) {
                    errorInvalidRequest.call()
                }
            })
        compositeDisposable.add(subscribe)
    }

    fun tryAuthWithPhoneNumByCode(code: String?) {
        interactor.tryAuthWithPhoneNumByCode(code)
    }

    fun onCharTyped(code: String) {
        if (code.length == 6) {
            btnAuthVisibility.setValue(true)
        } else if (btnAuthVisibility.value!!) {
            btnAuthVisibility.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        interactor.removeListeners()
    }

    fun onResendCodeClick() {
        interactor.resendCode()
        startTimer()
    }

    private fun startTimer() {
        enableResendCode.value = false
        object : CountDownTimer(TIME_OUT_MILLISECONDS.toLong(), 100) {
            override fun onTick(leftTimeInMilliseconds: Long) {
                val barValue = leftTimeInMilliseconds / (TIME_OUT_MILLISECONDS * 1.0) * 100
                showProgressTimeOut.value = barValue.toInt()
            }

            override fun onFinish() {
                showProgressTimeOut.value = 0
                enableResendCode.value = true
            }
        }.start()
    }

    companion object {
        const val TIME_OUT_MILLISECONDS = 30000
    }

    init {
        startTimer()
    }
}