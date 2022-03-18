package com.denchic45.appVersion

import com.denchic45.kts.BuildConfig

abstract class AppVersionService {

    var onUpdateDownloaded: () -> Unit = {}

    var onUpdateLoading: (progress: Long, totalMegabyte: Long) -> Unit = { _, _ -> }

    abstract fun observeUpdates(onUpdateAvailable: () -> Unit, onError: (Throwable) -> Unit)

    abstract fun observeDownloadedUpdate()

    abstract fun startDownloadUpdate()

    abstract val latestVersion: Int

    abstract fun installUpdate()

    fun isOldCurrentVersion(): Boolean = BuildConfig.VERSION_CODE < latestVersion
}