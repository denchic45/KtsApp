package com.denchic45.kts.ui.confirm

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ConfirmInteractor {

    private lateinit var eventChannel: (Boolean) -> Unit

    fun onConfirm(confirm: Boolean) = eventChannel(confirm)

    suspend fun awaitConfirm(): Boolean {
        return suspendCancellableCoroutine { continuation: CancellableContinuation<Boolean> ->
            eventChannel = {
                continuation.resume(it)
            }
        }
    }
}