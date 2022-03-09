package com.denchic45.kts.ui.login.resetPassword

import androidx.lifecycle.viewModelScope
import com.denchic45.kts.SingleLiveData
import com.denchic45.kts.data.repository.AuthRepository
import com.denchic45.kts.ui.base.BaseViewModel
import com.denchic45.kts.utils.Validations
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            try {
                authRepository.resetPassword(email)
                showSnackBar("Письмо успешно отправлено на почту")
                finish()
            } catch (t: Throwable) {
                showErrorFieldEmail.value = true
                showSnackBar("Произошла ошибка! Возможно неверная почта")
            }
        }
    }
}