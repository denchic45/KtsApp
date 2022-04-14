package com.denchic45.kts.ui.login.verifyPhoneNum

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.ui.base.BaseViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

class VerifyPhoneNumViewModel @Inject constructor(
    private val interactor: VerifyPhoneNumInteractor
) : BaseViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val btnAuthVisibility = SingleLiveData(false)

    private val _authSuccessful = Channel<String>()
    val authSuccessful = _authSuccessful.receiveAsFlow()

    val errorToManyRequest: SingleLiveData<*> = SingleLiveData<Any>()

    val errorInvalidRequest: SingleLiveData<*> = SingleLiveData<Any>()

    val showProgressTimeOut = MutableLiveData<Int>()

    val enableResendCode = MutableLiveData<Boolean>()

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