package com.denchic45.kts.ui.splash

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.denchic45.kts.data.repository.AuthRepository
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    authRepository: AuthRepository
) : ViewModel() {
    @JvmField
    var nextScreenMLiveData = MutableLiveData<NextActivity>()

    enum class NextActivity { MAIN, LOGIN }

    init {
        if (authRepository.currentUser != null) {
            nextScreenMLiveData.setValue(NextActivity.MAIN)
        } else {
            nextScreenMLiveData.setValue(NextActivity.LOGIN)
        }
    }
}