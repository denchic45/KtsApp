package com.denchic45.kts.util

import android.content.Context
import android.net.Uri
import android.widget.Toast

fun Context.path(uri: Uri): String = AndroidFiles.getPath(this, uri)

fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()

fun Context.toast(messageRes: Int, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, getString(messageRes), duration).show()