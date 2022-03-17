package com.denchic45.appVersion

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import java.io.Closeable
import java.lang.ref.WeakReference

class GoogleAppVersionService constructor(context: Context) : AppVersionService(), Closeable {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    private val info by lazy { appUpdateManager.appUpdateInfo }

    override var onUpdateDownloaded: () -> Unit = {}

    override var onUpdateLoading: (progress: Int, megabyteTotal: Int) -> Unit = { _, _ -> }

    var activityRef: WeakReference<Activity> = WeakReference(null)

    companion object {
        const val UPDATE_REQUEST_CODE = 1
    }

    override fun observeUpdates(onUpdateAvailable: () -> Unit, onError: (Throwable) -> Unit) {
        Log.d("lol", "observeUpdates: ")
        info.addOnSuccessListener { appUpdateInfo ->
            Log.d("lol", "observeUpdates: updateAvailability ${appUpdateInfo.updateAvailability()}")
            Log.d("lol", "observeUpdates: installStatus ${appUpdateInfo.installStatus()}")
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                onUpdateDownloaded()
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            ) {
                Log.d("lol", "observeUpdates: update")
                onUpdateAvailable()
            }

        }.addOnFailureListener {
            Log.d("lol", "observeUpdates: error")
            it.printStackTrace()
            onError(it)
        }
    }

    override fun observeDownloadedUpdate() {
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { appUpdateInfo ->
                Log.d(
                    "lol",
                    "observeDownloadedUpdate installStatus: ${appUpdateInfo.installStatus()}"
                )
                Log.d(
                    "lol",
                    "observeDownloadedUpdate availableVersionCode: ${appUpdateInfo.availableVersionCode()}"
                )
                // If the update is downloaded but not installed,
                // notify the user to complete the update.
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    onUpdateDownloaded()
                }
            }
    }

    override val latestVersion: Int
        get() = info.result.availableVersionCode()

    override fun startUpdate() {
        Log.d("lol", "startUpdate:")
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            info.result,
            // Or 'AppUpdateType.IMMEDIATE for immediate updates.
            AppUpdateType.FLEXIBLE,
            // The current activity.
            activityRef.get()!!,
            UPDATE_REQUEST_CODE
        )

        val listener = InstallStateUpdatedListener { state ->
            // (Optional) Provide a download progress bar.
            Log.d("lol", "startUpdate state: ${state.installStatus()}")
            when (state.installStatus()) {
                InstallStatus.DOWNLOADING -> {
                    val bytesDownloaded = state.bytesDownloaded()
                    val totalBytesToDownload = state.totalBytesToDownload()
                    // Show update progress bar.
                }
                InstallStatus.DOWNLOADED -> {
                    Log.d("lol", "startUpdate: DOWNLOADED")
                    onUpdateDownloaded()
                }
                InstallStatus.FAILED -> {
                    Log.d("lol", "startUpdate: FAILED ${state.installErrorCode()}")
                }
            }
            // Log state or install the update.
        }

// Before starting an update, register a listener for updates.
        appUpdateManager.registerListener(listener)

// Start an update.

// When status updates are no longer needed, unregister the listener.
//        appUpdateManager.unregisterListener(listener)
    }

    override fun installUpdate() {
        Log.d("lol", "installUpdate: ")
        appUpdateManager.completeUpdate()
    }

    override fun close() {
        activityRef.clear()
    }
}
