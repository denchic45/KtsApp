package com.denchic45.kts.utils

import android.content.Context
import android.net.Uri

fun Context.path(uri: Uri): String = Files.getPath(this, uri)
