package com.denchic45.kts.ui.login

import android.telephony.PhoneNumberUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denchic45.kts.R
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.rx.AsyncCompletableTransformer
import com.denchic45.kts.utils.Validations
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val interactor: LoginInteractor
) : ViewModel() {
    @JvmField
    val backToFragment = SingleLiveData<Void>()

    @JvmField
    val finishApp = SingleLiveData<Void>()

    @JvmField
    val openMain: SingleLiveData<*> = SingleLiveData<Any>()

    @JvmField
    val errorNum = SingleLiveData<String>()

    val toolbarTitle = MutableLiveData<String>()

    @JvmField
    val showProgress = MutableLiveData(0f)

    @JvmField
    val fabVisibility = MutableLiveData<Boolean>()

    @JvmField
    val verifyUser = MutableLiveData<String>()

    @JvmField
    val showMailError = SingleLiveData<String?>()

    @JvmField
    val showPasswordError = SingleLiveData<String?>()

    @JvmField
    val openLoginByPhoneNum = SingleLiveData<Void>()

    @JvmField
    val openLoginByMail = SingleLiveData<Void>()

    @JvmField
    val openVerifyPhoneNum = SingleLiveData<Void>()

    @JvmField
    val openResetPassword = SingleLiveData<Void>()

    @JvmField
    val showMessage = SingleLiveData<String>()
    private val addedProgress = Stack<Float?>()
    private val testNumbers = Stream.of(arrayOf("+16505553434", "+79510832144")).collect(
        Collectors.toMap(
            { data: Array<String> -> data[0] },
            { data: Array<String> -> data[1] })
    )
    var userAccountList: LiveData<List<User>>? = null
    fun onGetCodeClick(phoneNum: String) {
        val normalizeNumber = PhoneNumberUtils.normalizeNumber(phoneNum)
        val realProneNum = testNumbers[normalizeNumber] ?: normalizeNumber
        interactor.findUserByPhoneNum(realProneNum)
            .compose(AsyncCompletableTransformer())
            .subscribe(
                {
                    fabVisibility.value = false
                    incrementProgress(0.65f)
                    toolbarTitle.value = "Проверка"
                    openVerifyPhoneNum.call()
                    verifyUser.setValue(normalizeNumber)
                }
            ) { throwable: Throwable ->
                if (throwable is FirebaseNetworkException) {
                    showMessage.setValue(throwable.message)
                } else {
                    errorNum.setValue(throwable.message)
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
        if (!Validations.isValidEmail(mail)) {
            showMailError.value = "Неккоректный ввод"
            return
        }
        interactor.authByEmail(mail, password)
            .subscribe(
                { onSuccessfulLogin() }
            ) { throwable: Throwable? ->
                if (throwable is FirebaseAuthException) {
                    when (throwable.errorCode) {
                        "ERROR_USER_NOT_FOUND" -> {
                            showMailError.value = "Пользователя с этой почтой не существует"
                            showPasswordError.setValue(null)
                        }
                        "ERROR_WRONG_PASSWORD" -> {
                            showMailError.value = null
                            showPasswordError.setValue("Неверный пароль")
                        }
                    }
                } else if (throwable is FirebaseNetworkException) {
                    showMessage.value = "Отсутствует интернет-соединение"
                }
            }
    }

    init {
        toolbarTitle.value = "Начало"
    }
}