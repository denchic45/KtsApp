package com.denchic45.kts.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

class Permissions {
    companion object {
        fun checkExternalStorageManager(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Environment.isExternalStorageManager()
            } else {
                val hasStoragePermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                hasStoragePermission == PackageManager.PERMISSION_GRANTED
            }
        }
    }
}
