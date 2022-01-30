package com.denchic45.kts.ui.confirm

import androidx.lifecycle.ViewModel
import com.denchic45.kts.rx.bus.RxBusConfirm

class ConfirmViewModel : ViewModel() {
    fun onNegativeClick() {
        RxBusConfirm.getInstance().postEvent(false)
    }

    fun onPositiveClick() {
        RxBusConfirm.getInstance().postEvent(true)
    }

    override fun onCleared() {
        super.onCleared()
        RxBusConfirm.getInstance().completeEvent()
    }
}