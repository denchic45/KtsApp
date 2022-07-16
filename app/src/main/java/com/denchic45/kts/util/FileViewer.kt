package com.denchic45.kts.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import java.io.File

class FileViewer(private val activity: Activity, private val onNotFoundActivity: () -> Unit) {
    fun openFile(file: File) {
        // Get URI and MIME type of file
        // Open file with user selected app
        val intent = Intent().apply {
            action = Intent.ACTION_VIEW
            val apkURI = FileProvider.getUriForFile(
                activity,
                activity.applicationContext
                    .packageName.toString() + ".provider", file
            )
            val extensionFromMimeType = MimeTypeMap
                .getSingleton()
                .getExtensionFromMimeType(file.toURI().toURL().toString())
            setDataAndType(apkURI, extensionFromMimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            activity.startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            onNotFoundActivity()
        }
    }
}