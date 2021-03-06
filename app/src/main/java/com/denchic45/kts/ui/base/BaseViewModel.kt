package com.denchic45.kts.ui.base

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDirections
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel(), DefaultLifecycleObserver {

    val finish = MutableSharedFlow<Nothing?>()

    val toast = MutableSharedFlow<String>()

    val toastRes = MutableSharedFlow<Int>()

    val snackBar = MutableSharedFlow<Pair<String, String?>>()

    val snackBarRes = MutableSharedFlow<Pair<Int, Int?>>()

    val dialog = MutableSharedFlow<Pair<String, String>>()

    val dialogRes = MutableSharedFlow<Pair<Int, Int>>()

    private val _openConfirmation = Channel<Pair<String, String>>()

    val openConfirmation = _openConfirmation.receiveAsFlow()

    internal val showToolbarTitle = MutableSharedFlow<String>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val navigate = MutableSharedFlow<NavigationCommand>()

    fun navigateTo(navDirections: NavDirections) {
        viewModelScope.launch {
            navigate.emit(NavigationCommand.To(navDirections))
        }
    }

//    val optionsVisibility:Map<Int, Boolean> = mapOf()

    val optionsVisibility = MutableStateFlow<Map<Int, Boolean>>(emptyMap())

    fun setMenuItemVisible(itemIdWithVisible: Pair<Int, Boolean>) {
        optionsVisibility.update { it + itemIdWithVisible }
    }

    fun setMenuItemVisible(vararg itemIdWithVisible: Pair<Int, Boolean>) {
        optionsVisibility.update { it + itemIdWithVisible }
    }

    protected var toolbarTitle: String
        get() = showToolbarTitle.replayCache[0]
        set(value) {
            showToolbarTitle.tryEmit(value)
        }

    fun setTitle(name: String) {
        showToolbarTitle.tryEmit(name)
    }

    protected suspend fun finish() {
        finish.emit(null)
    }

    protected fun openConfirmation(titleWithText: Pair<String, String>) {
        viewModelScope.launch {
            _openConfirmation.send(titleWithText)
        }
    }

    protected fun showToast(message: String) {
        viewModelScope.launch { toast.emit(message) }
    }

    protected fun showToast(messageRes: Int) {
        viewModelScope.launch { toastRes.emit(messageRes) }
    }

    protected fun showSnackBar(message: String, action: String? = null) {
        viewModelScope.launch { snackBar.emit(message to action) }
    }

    protected fun showSnackBar(messageRes: Int, actionRes: Int? = null) {
        viewModelScope.launch { snackBarRes.emit(messageRes to actionRes) }
    }

    protected fun showDialog(title: String, message: String) {
        viewModelScope.launch { dialog.emit(Pair(title, message)) }
    }

    protected fun showDialog(title: Int, message: Int) {
        viewModelScope.launch { dialogRes.emit(Pair(title, message)) }
    }

    private var optionsIsCreated: Boolean = false

    open fun onCreateOptions() {
        if (optionsIsCreated) return
        else optionsIsCreated = true
    }

    open fun onOptionClick(itemId: Int) {}

    open fun onSnackbarActionClick(message: String) {}
}