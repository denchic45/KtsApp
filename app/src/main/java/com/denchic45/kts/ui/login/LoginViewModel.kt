package com.denchic45.kts.ui.login

import android.telephony.PhoneNumberUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.Validations
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val interactor: LoginInteractor
) : BaseViewModel() {

    val backToFragment = SingleLiveData<Void>()

    val finishApp = SingleLiveData<Void>()

    val openMain: SingleLiveData<*> = SingleLiveData<Any>()

    val errorNum = SingleLiveData<String>()

    val showProgress = MutableLiveData(0f)

    val fabVisibility = MutableLiveData<Boolean>()

    val verifyUser = MutableLiveData<String>()

    val showMailError = SingleLiveData<String?>()

    val showPasswordError = SingleLiveData<String?>()

    val openLoginByPhoneNum = SingleLiveData<Void>()

    val openLoginByMail = SingleLiveData<Void>()

    val openVerifyPhoneNum = SingleLiveData<Void>()

    val openResetPassword = SingleLiveData<Void>()

    private val addedProgress = Stack<Float>()
    private val testNumbers = Stream.of(arrayOf("+16505553434", "+79510832144")).collect(
        Collectors.toMap(
            { data: Array<String> -> data[0] },
            { data: Array<String> -> data[1] })
    )

    fun onGetCodeClick(phoneNum: String) {
        val normalizeNumber = PhoneNumberUtils.normalizeNumber(phoneNum)
        val realProneNum = testNumbers[normalizeNumber] ?: normalizeNumber
        viewModelScope.launch {
            try {
                interactor.findUserByPhoneNum(realProneNum)

                fabVisibility.value = false
                incrementProgress(0.65f)
                toolbarTitle = "Проверка"
                openVerifyPhoneNum.call()
                verifyUser.setValue(normalizeNumber)
            } catch (t: Throwable) {
                if (t is FirebaseNetworkException) {
                    showToast(t.message!!)
                } else {
                    errorNum.setValue(t.message)
                }
            }
        }
    }

    fun onSmsClick() {
        incrementProgress(0.35f)
        fabVisibility.value = true
        openLoginByPhoneNum.call()
    }

    private fun incrementProgress(progress: Float) {
        addedProgress.push(showProgress.value)
        showProgress.value = progress
    }

    fun onEmailClick() {
        incrementProgress(0.5f)
        openLoginByMail.call()
        fabVisibility.value = true
    }

    fun onForgotPasswordClick() {
        incrementProgress(0.5f)
        openResetPassword.call()
    }

    fun onSuccessfulLogin() {
        incrementProgress(1f)
        openMain.call()
    }

    fun onFabBackClick(id: Int) {
        if (addedProgress.isEmpty()) {
            finishApp.call()
            return
        }
        showProgress.value = addedProgress.pop()
        if (id == R.id.loginByPhoneNumFragment || id == R.id.loginByEmailFragment) {
            fabVisibility.value = false
        }
        backToFragment.call()
    }

    fun onNextMailClick(mail: String, password: String) {
        if (Validations.notValidEmail(mail)) {
            showMailError.value = "Неккоректный ввод"
            return
        }
        viewModelScope.launch {
            try {
                interactor.authByEmail(mail, password)
                onSuccessfulLogin()
            } catch (t: Throwable) {
                t.printStackTrace()
                if (t is FirebaseAuthException) {
                    when (t.errorCode) {
                        "ERROR_USER_NOT_FOUND" -> {
                            showMailError.value = "Пользователя с этой почтой не существует"
                            showPasswordError.setValue(null)
                        }
                        "ERROR_WRONG_PASSWORD" -> {
                            showMailError.value = null
                            showPasswordError.setValue("Неверный пароль")
                        }
                    }
                } else if (t is FirebaseNetworkException) {
                    showToast(R.string.error_check_network)
                }
            }
        }
    }

    init {
        toolbarTitle = "Начало"
    }
}