package com.denchic45.appVersion

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import java.lang.ref.WeakReference
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GoogleAppVersionService @Inject constructor(
    context: Context
) : AppVersionService(), Closeable {

    private val appUpdateManager = AppUpdateManagerFactory.create(context)

    private val info by lazy { appUpdateManager.appUpdateInfo }

    var activityRef: WeakReference<Activity> = WeakReference(null)

    val listener: InstallStateUpdatedListener = InstallStateUpdatedListener { state ->
        Log.d("lol", "startUpdate state: ${state.installStatus()}")
        when (state.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                Log.d(
                    "lol",
                    "startUpdate DOWNLOADING: mega bytes downloaded ${state.bytesDownloaded()} and total ${state.totalBytesToDownload()}"
                )
                val megaBytesDownloaded = state.bytesDownloaded().toFloat() / 1024 / 1024


                val totalMegaBytesToDownload =
                    NumberFormat.getInstance(Locale.getDefault()).parse(
                        String.format(
                            "%.2f",
                            state.totalBytesToDownload().toFloat() / 1024 / 1024
                        )
                    )!!.toFloat()

                val percentDownload = megaBytesDownloaded / totalMegaBytesToDownload * 100
                Log.d(
                    "lol",
                    "startUpdate DOWNLOADING: $percentDownload $totalMegaBytesToDownload"
                )
                onUpdateLoading(percentDownload.toLong(), totalMegaBytesToDownload)
            }
            InstallStatus.DOWNLOADED -> {
                Log.d("lol", "startUpdate: DOWNLOADED")
                onUpdateDownloaded()
            }
            InstallStatus.FAILED -> {
                Log.d("lol", "startUpdate: FAILED ${state.installErrorCode()}")
            }
        }
    }

    override suspend fun getLatestVersion(): Int {
        return suspendCancellableCoroutine { cont ->
            info.addOnCompleteListener {
                Log.d("lol", "getLatestVersion addOnCompleteListener: ")
                val e = it.exception
                if (e == null) {
                    Log.d("lol", "getLatestVersion resume: ")
                    @Suppress("UNCHECKED_CAST") cont.resume(it.result.availableVersionCode())
                } else {
                    Log.d("lol", "getLatestVersion error: ")
                    cont.resumeWithException(e)
                }
            }
        }
    }

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
                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                    onUpdateDownloaded()
                }
            }
    }

    override fun startDownloadUpdate() {
        Log.d("lol", "startUpdate:")
        appUpdateManager.registerListener(listener)
        appUpdateManager.startUpdateFlowForResult(
            info.result,
            AppUpdateType.FLEXIBLE,
            // The current activity.
            activityRef.get()!!,
            UPDATE_REQUEST_CODE
        )
    }

    override fun installUpdate() {
        Log.d("lol", "installUpdate: ")
        appUpdateManager.completeUpdate()
    }

    override fun close() {
        appUpdateManager.unregisterListener(listener)
        activityRef.clear()
    }
}
