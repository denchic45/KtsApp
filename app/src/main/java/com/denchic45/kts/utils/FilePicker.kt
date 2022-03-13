package com.denchic45.kts.utils

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.denchic45.kts.ui.adminPanel.timetableEditor.loader.TimetableLoaderFragment
import org.apache.poi.util.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class FilePicker(
    private val fragment: Fragment,
    private val callback: (files: List<File>?) -> Unit,
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
        ) {
            if (it.resultCode == Activity.RESULT_OK) {
                it.data?.let { data: Intent ->
                    data.clipData?.let { clipData: ClipData ->
                        callback(
                            List(clipData.itemCount) { position ->
                                clipData.getItemAt(position).uri.toFile()
                            }
                        )
                    } ?: run {

//                        fragment.requireContext().contentResolver.takePersistableUriPermission(
//                            data!!,
//                            Intent.FLAG_GRANT_READ_URI_PERMISSION
//                        )

                        callback(listOf(data.data!!.toFile()))
                    }
                }
            }
        }

    private fun Uri.toFile(): File {
        val context = this@FilePicker.fragment.requireContext()
        val parcelFileDescriptor = context.contentResolver.openFileDescriptor(this, "r", null)
        val inputStream = FileInputStream(parcelFileDescriptor!!.fileDescriptor)

        val file = File(context.cacheDir, context.contentResolver.getFileName(this))
        val outputStream = FileOutputStream(file)
        IOUtils.copy(inputStream, outputStream)
        return file
    }

    private fun ContentResolver.getFileName(fileUri: Uri): String {

        var name = ""
        val returnCursor = this.query(fileUri, null, null, null, null)
        if (returnCursor != null) {
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()
            name = returnCursor.getString(nameIndex)
            returnCursor.close()
        }

        return name
    }

    fun selectFiles() {
        chooseFile()

//        if (Permissions.checkExternalStorageManager(activityFromFragment())) {
//            chooseFile()
//        } else {
//            requestPermissions()
//        }
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
        var chooserIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
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