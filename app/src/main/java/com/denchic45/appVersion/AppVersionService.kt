package com.denchic45.appVersion

import com.denchic45.kts.BuildConfig

abstract class AppVersionService {

    var onUpdateDownloaded: () -> Unit = {}

    var onUpdateLoading: (progress: Long, totalMegabyte: Float) -> Unit = { _, _ -> }

    abstract fun observeUpdates(onUpdateAvailable: () -> Unit, onError: (Throwable) -> Unit)

    abstract fun observeDownloadedUpdate()

    abstract fun startDownloadUpdate()

    abstract suspend fun getLatestVersion(): Int

    abstract fun installUpdate()

    suspend fun isOldCurrentVersion(): Boolean {
        return BuildConfig.VERSION_CODE < getLatestVersion()
    }
}