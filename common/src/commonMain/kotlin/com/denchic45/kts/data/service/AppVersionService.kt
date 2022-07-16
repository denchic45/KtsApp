package com.denchic45.kts.data.service


abstract class AppVersionService {

    var onUpdateDownloaded: () -> Unit = {}

    var onUpdateLoading: (progress: Long, totalMegabyte: Float) -> Unit = { _, _ -> }

    abstract fun observeUpdates(onUpdateAvailable: () -> Unit, onError: (Throwable) -> Unit)

    abstract fun observeDownloadedUpdate()

    abstract fun startDownloadUpdate()

    abstract suspend fun getLatestVersion(): Int

    abstract fun installUpdate()

    //TODO исправить! Проверять, не устарела ли версия
    suspend fun isOldCurrentVersion(): Boolean {
        return false
//        return BuildConfig.VERSION_CODE < getLatestVersion()
    }
}