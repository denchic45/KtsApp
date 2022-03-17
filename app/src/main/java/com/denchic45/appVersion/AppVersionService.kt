package com.denchic45.appVersion

import com.denchic45.kts.BuildConfig

abstract class AppVersionService {

    abstract var onUpdateDownloaded: () -> Unit

    abstract var onUpdateLoading: (progress: Int, megabyteTotal: Int) -> Unit

    abstract fun observeUpdates(onUpdateAvailable: () -> Unit, onError: (Throwable) -> Unit)

    abstract fun observeDownloadedUpdate()

    abstract fun startUpdate()

    abstract val latestVersion: Int

    abstract fun installUpdate()

    fun isOldCurrentVersion(): Boolean = BuildConfig.VERSION_CODE < latestVersion
}