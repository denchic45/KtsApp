package com.denchic45.kts.ui.settings

import androidx.lifecycle.ViewModel
import com.denchic45.kts.data.repository.AuthRepository
import javax.inject.Inject

class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    fun onSignOutCLick() {
        authRepository.signOut()
    }
}