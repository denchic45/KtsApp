package com.denchic45.kts.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    val finish = MutableSharedFlow<Nothing?>()

    val toast = MutableSharedFlow<String>()

    val toastRes = MutableSharedFlow<Int>()

    val snackBar = MutableSharedFlow<String>()

    val snackBarRes = MutableSharedFlow<Int>()

    val dialog = MutableSharedFlow<Pair<String, String>>()

    val dialogRes = MutableSharedFlow<Pair<Int, Int>>()

    private val _openConfirmation = Channel<Pair<String, String>>()

    val openConfirmation = _openConfirmation.receiveAsFlow()

    internal val showToolbarTitle = MutableStateFlow<String>("")

    val optionVisibility = MutableSharedFlow<Pair<Int, Boolean>>()

    protected var toolbarTitle: String
        get() = showToolbarTitle.value
        set(value) {
                showToolbarTitle.value = value
        }

    protected fun finish() {
        viewModelScope.launch { finish.emit(null) }
    }

    protected fun openConfirmation(titleWithText: Pair<String, String>) {
        viewModelScope.launch {
            _openConfirmation.send(titleWithText)
        }
    }

    protected fun showToast(message: String) {
       viewModelScope.launch {  toast.emit(message) }
    }

    protected fun showToast(messageRes: Int) {
        viewModelScope.launch {  toastRes.emit(messageRes) }
    }

    protected fun showSnackBar(message: String) {
        viewModelScope.launch {  snackBar.emit(message) }
    }

    protected fun showSnackBar(messageRes: Int) {
        viewModelScope.launch {  snackBarRes.emit(messageRes) }
    }

    protected fun showDialog(title: String, message: String) {
        viewModelScope.launch {  dialog.emit(Pair(title, message)) }
    }

    protected fun showDialog(title: Int, message: Int) {
        viewModelScope.launch {  dialogRes.emit(Pair(title, message)) }
    }

    private var optionsIsCreated: Boolean = false

    open fun onCreateOptions() {
        if (optionsIsCreated) return
        else optionsIsCreated = true
    }

    open fun onOptionClick(itemId: Int) {}
}