package com.denchic45.kts.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.SingleLiveData
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    @JvmField
    val showMessage = SingleLiveData<String>()

    @JvmField
    val showMessageRes = SingleLiveData<Int>()

    @JvmField
    val finish = SingleLiveData<Void>()

    val showToast = MutableSharedFlow<String>()

    val openConfirmation = SingleLiveData<Pair<String, String>>()

    internal val showToolbarTitle = MutableSharedFlow<String>(replay = 1)

    protected var toolbarTitle: String
        get() = showToolbarTitle.replayCache[0]
        set(value) {
            viewModelScope.launch {
                showToolbarTitle.emit(value)
            }
        }

//    protected fun setToolbarTitle(title: String) {
//        viewModelScope.launch {
//            _toolbarTitle.emit(title)
//        }
//    }

    private var optionsIsCreated: Boolean = false

    open fun onCreateOptions() {
        if (optionsIsCreated) return
        else optionsIsCreated = true
    }

    open fun onOptionClick(itemId: Int) {}
}