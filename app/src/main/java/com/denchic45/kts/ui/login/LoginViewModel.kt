package com.denchic45.kts.ui.login

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.util.Validations
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import kotlinx.coroutines.launch
import java.util.*
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

    val showMailError = SingleLiveData<String?>()

    val showPasswordError = SingleLiveData<String?>()

    val openLoginByMail = SingleLiveData<Void>()

    val openVerifyPhoneNum = SingleLiveData<Void>()

    val openResetPassword = SingleLiveData<Void>()

    private val addedProgress = Stack<Float>()

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
        Log.d("lol", "A openMain: ")
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
                Log.d("lol", "A try authByEmail: ")
                interactor.authByEmail(mail, password)
                Log.d("lol", "A onSuccessfulLogin: ")
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