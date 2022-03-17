package com.denchic45.kts

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.api.LogDescriptor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppVersionService @Inject constructor(context: Context) {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    private val info = appUpdateManager.appUpdateInfo

    var onUpdateDownloaded: () -> Unit = {}

    companion object {
        const val UPDATE_REQUEST_CODE = 1
    }

    fun observeUpdates(onUpdateAvailable: () -> Unit, onError: (Throwable)->Unit) {
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
               onError( it)
            }
    }

    fun startUpdate(activity: Activity) {
        Log.d("lol", "startUpdate:")
        appUpdateManager.startUpdateFlowForResult(
            // Pass the intent that is returned by 'getAppUpdateInfo()'.
            info.result,
            // Or 'AppUpdateType.IMMEDIATE for immediate updates.
            AppUpdateType.FLEXIBLE,
            // The current activity.
            activity,
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

    fun installUpdate() {
        Log.d("lol", "installUpdate: ")
        appUpdateManager.completeUpdate()
    }
}
