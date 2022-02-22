package com.denchic45.kts.ui.confirm

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class ConfirmInteractor {

    private lateinit var continuation: CancellableContinuation<Boolean>

    fun onConfirm(confirm: Boolean) {
        continuation.resume(confirm)
    }

    suspend fun awaitConfirm(): Boolean {
        return suspendCancellableCoroutine { continuation: CancellableContinuation<Boolean> ->
            this.continuation = continuation
        }
    }
}