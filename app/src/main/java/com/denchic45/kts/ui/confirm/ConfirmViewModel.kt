package com.denchic45.kts.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.denchic45.kts.rx.bus.RxBusConfirm
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfirmViewModel @Inject constructor(
    private val confirmInteractor: ConfirmInteractor
) : ViewModel() {
    fun onNegativeClick() {
        RxBusConfirm.getInstance().postEvent(false)
            confirmInteractor.onConfirm(false)
    }

    fun onPositiveClick() {
        RxBusConfirm.getInstance().postEvent(true)
            confirmInteractor.onConfirm(true)
    }

    override fun onCleared() {
        super.onCleared()
        RxBusConfirm.getInstance().completeEvent()
    }
}