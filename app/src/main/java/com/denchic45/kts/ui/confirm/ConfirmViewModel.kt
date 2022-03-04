package com.denchic45.kts.ui.confirm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ConfirmViewModel @Inject constructor(
    private val confirmInteractor: ConfirmInteractor
) : ViewModel() {
    fun onNegativeClick() {
        viewModelScope.launch {
            confirmInteractor.onConfirm(false)
        }
    }

    fun onPositiveClick() {
        viewModelScope.launch {
            confirmInteractor.onConfirm(true)
        }
    }
}