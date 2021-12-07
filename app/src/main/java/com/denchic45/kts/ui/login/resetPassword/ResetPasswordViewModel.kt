package com.denchic45.kts.ui.login.resetPassword

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.repository.AuthRepository
import com.denchic45.kts.utils.Validations
import javax.inject.Inject

class ResetPasswordViewModel @Inject constructor(
    application: Application,
    private val authRepository: AuthRepository
) : AndroidViewModel(
     application
) {
    @JvmField
    val showErrorFieldEmail = SingleLiveData<Boolean>()

    @JvmField
    val finish = SingleLiveData<Void>()

    @JvmField
    var showMessage = SingleLiveData<String>()
    fun onSendClick(email: String) {
        if (!Validations.isValidEmail(email)) {
            showErrorFieldEmail.value = true
            return
        }
        showErrorFieldEmail.value = false
        authRepository.resetPassword(email)
            .subscribe(
                {
                    showMessage.value = "Письмо успешно отправлено на почту"
                    finish.call()
                }
            ) {
                showErrorFieldEmail.value = true
                showMessage.setValue("Произошла ошибка! Возможно неверная почта")
            }
    }
}