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
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.loader.TimetableLoaderFragment

class FilePicker(
    private val fragment: Fragment,
    private val callback: (activityResult: ActivityResult) -> Unit,
    private val multipleSelect: Boolean = false
) {

    private val registry: ActivityResultRegistry = activityFromFragment().activityResultRegistry

    private fun activityFromFragment() = fragment.requireActivity()

    private var getPermissions: ActivityResultLauncher<Intent> =
        registry.register(
            PERMISSION_REGISTRY_KEY,
            fragment,
            ActivityResultContracts.StartActivityForResult()
        ) { chooseFile() }

    private var getFile: ActivityResultLauncher<Intent> =
        registry.register(
            RESULT_REGISTRY_KEY + fragment.javaClass.name,
            fragment,
            ActivityResultContracts.StartActivityForResult()
        ) { callback(it) }

    fun selectFiles() {
        if (Permissions.checkExternalStorageManager(activityFromFragment())) {
            chooseFile()
        } else {
            requestPermissions()
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                getPermissions.launch(Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    addCategory("android.intent.category.DEFAULT")
                    data = Uri.parse(
                        String.format(
                            "package:%s",
                            activityFromFragment().applicationContext.packageName
                        )
                    )
                })
            } catch (e: Exception) {
                getPermissions.launch(Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION))
            }
        } else {
            ActivityCompat.requestPermissions(
                activityFromFragment(), arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), TimetableLoaderFragment.PICK_FILE_RESULT_CODE
            )
            chooseFile()
        }
    }

    private fun chooseFile() {
        var chooserIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            if (multipleSelect) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
        }

        chooserIntent = Intent.createChooser(
            chooserIntent,
            "Выберите документ с расписанием за" + " N " + "курс"
        )
        getFile.launch(chooserIntent)
    }

    private companion object {

        const val PERMISSION_REGISTRY_KEY = "permission_file"
        const val RESULT_REGISTRY_KEY = "pick_file"
    }
}