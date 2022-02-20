package com.denchic45.kts.ui.login.resetPassword

import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.repository.AuthRepository
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.Validations
import javax.inject.Inject

class ResetPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {

    val showErrorFieldEmail = SingleLiveData<Boolean>()
    fun onSendClick(email: String) {
        if (Validations.notValidEmail(email)) {
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