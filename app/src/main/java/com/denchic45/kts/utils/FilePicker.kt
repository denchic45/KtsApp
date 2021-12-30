package com.denchic45.kts.utils

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import com.denchic45.kts.ui.adminPanel.timtableEditor.loader.TimetableLoaderFragment

class FilePicker(
    private val activity: AppCompatActivity,
    private val owner: LifecycleOwner,
    private val callback: (activityResult: ActivityResult) -> Unit
) {

    private val registry: ActivityResultRegistry = activity.activityResultRegistry

    private var getPermissions: ActivityResultLauncher<Intent> =
        registry.register(
            PERMISSION_REGISTRY_KEY,
            owner,
            ActivityResultContracts.StartActivityForResult(),
            { chooseFile() })

    private var getFile: ActivityResultLauncher<Intent> =
        registry.register(
            RESULT_REGISTRY_KEY,
            owner,
            ActivityResultContracts.StartActivityForResult(),
            { callback(it) })

    fun selectFiles() {
//        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
//            return
//        }

        if (Permissions.checkExternalStorageManager(activity)) {
            chooseFile()
        } else {
            requestPermissions()
        }

//        requestPermissions()
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                getPermissions.launch(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    addCategory("android.intent.category.DEFAULT")
                    data = Uri.parse(
                        String.format(
                            "package:%s",
                            activity.applicationContext.packageName
                        )
                    )
                })
            } catch (e: Exception) {
                getPermissions.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else {
            ActivityCompat.requestPermissions(
                activity, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), TimetableLoaderFragment.PICK_FILE_RESULT_CODE
            )
            chooseFile()
        }
    }

    private fun chooseFile() {
        var chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.type = "*/*"
        chooseFile = Intent.createChooser(
            chooseFile,
            "Выберите документ с расписанием за" + " N " + "курс"
        )
        getFile.launch(chooseFile)
    }

    private companion object {

        const val PERMISSION_REGISTRY_KEY = "permission_file"
        const val RESULT_REGISTRY_KEY = "pick_file"
    }
}