package com.denchic45.kts.ui.base

import androidx.lifecycle.ViewModel
import com.denchic45.kts.SingleLiveData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

open class BaseViewModel : ViewModel() {
    @JvmField
    val showMessage = SingleLiveData<String>()

    @JvmField
    val showMessageRes = SingleLiveData<Int>()

    @JvmField
    val finish = SingleLiveData<Void>()


    val showToast = MutableSharedFlow<String>()

    val openConfirmation = SingleLiveData<Pair<String, String>>()

    private var optionsIsCreated: Boolean = false

    open fun onCreateOptions() {
        if (optionsIsCreated) return
        else optionsIsCreated = true
    }

}