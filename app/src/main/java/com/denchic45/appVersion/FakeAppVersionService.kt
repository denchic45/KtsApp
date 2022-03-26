package com.denchic45.appVersion

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class FakeAppVersionService @Inject constructor(
    private val coroutineScope: CoroutineScope
) : AppVersionService() {

    override fun observeUpdates(onUpdateAvailable: () -> Unit, onError: (Throwable) -> Unit) {
        onUpdateAvailable()
    }

    override fun observeDownloadedUpdate() {

    }

    override fun startDownloadUpdate() {
        coroutineScope.launch(Dispatchers.IO) {
            onUpdateLoading(0,10F)
            delay(500)
            onUpdateLoading(23,10F)
            delay(1000)
            onUpdateLoading(24,10F)
            delay(600)
            onUpdateLoading(56,10F)
            delay(1000)
            onUpdateLoading(100, 10F)
            delay(1500)
            onUpdateDownloaded()
        }
    }

    override suspend fun getLatestVersion(): Int {
        return 0
    }

    override fun installUpdate() {

    }
}