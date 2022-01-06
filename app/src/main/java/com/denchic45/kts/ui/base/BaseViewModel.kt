package com.denchic45.kts.ui.base

import androidx.lifecycle.ViewModel
import com.denchic45.kts.SingleLiveData

open class BaseViewModel : ViewModel() {
    @JvmField
    val showMessage = SingleLiveData<String>()

    @JvmField
    val showMessageRes = SingleLiveData<Int>()

    @JvmField
    val finish = SingleLiveData<Void>()

    val openConfirmation = SingleLiveData<Pair<String, String>>()
}