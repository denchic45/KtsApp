package com.denchic45.kts.utils

import android.content.Context
import android.net.Uri
import android.widget.Toast

fun Context.path(uri: Uri): String = Files.getPath(this, uri)

fun Context.toast(message: CharSequence, duration: Int = Toast.LENGTH_SHORT) =
    Toast.makeText(this, message, duration).show()