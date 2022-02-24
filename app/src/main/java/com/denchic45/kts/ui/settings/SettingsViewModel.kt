package com.denchic45.kts.ui.settings

import com.denchic45.kts.data.repository.AuthRepository
import com.denchic45.kts.ui.base.BaseViewModel
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : BaseViewModel() {
    fun onSignOutCLick() {
        authRepository.signOut()
    }
}