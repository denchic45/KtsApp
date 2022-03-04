package com.denchic45.kts.ui.confirm

import kotlinx.coroutines.channels.Channel

class ConfirmInteractor {

    private val confirmation = Channel<Boolean>()

    suspend fun onConfirm(confirm: Boolean) {
        confirmation.send(confirm)
    }

    suspend fun awaitConfirm(): Boolean {
        return confirmation.receive()
    }
}