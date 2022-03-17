package com.denchic45.appVersion

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class FakeAppVersionService @Inject constructor(
    private val coroutineScope: CoroutineScope
) : AppVersionService() {

    override var onUpdateDownloaded: () -> Unit = {}
    override var onUpdateLoading: (progress: Int, megabyteTotal: Int) -> Unit = { _, _ -> }

    override fun observeUpdates(onUpdateAvailable: () -> Unit, onError: (Throwable) -> Unit) {
        onUpdateAvailable()
    }

    override fun observeDownloadedUpdate() {

    }

    override fun startUpdate() {
        coroutineScope.launch(Dispatchers.IO) {
            onUpdateLoading(0,10)
            delay(500)
            onUpdateLoading(23,10)
            delay(1000)
            onUpdateLoading(24,10)
            delay(4000)
            onUpdateLoading(56,10)
            delay(1000)
            onUpdateLoading(94,10)
            delay(500)
            onUpdateLoading(100,10)
            delay(3000)
            onUpdateDownloaded()
        }
    }

    override val latestVersion: Int = 0

    override fun installUpdate() {

    }
}