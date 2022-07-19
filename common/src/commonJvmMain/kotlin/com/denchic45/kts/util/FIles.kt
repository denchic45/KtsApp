package com.denchic45.kts.util

import java.io.File
import java.io.FileOutputStream
import java.net.FileNameMap
import java.net.URLConnection
import kotlin.math.min


fun File.getType() = getMimeType().split("/")[0]

fun File.getMimeType(): String {
    val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
    val mime = fileNameMap.getContentTypeFor(this.name) ?: return ""
    return if (mime.split("/")[0] == "application") {
        return if (mime.contains(".")) {
            mime.substring(mime.lastIndexOf(".") + 1)
        } else {
            mime.substring(mime.indexOf('/') + 1)
        }
    } else mime.split("/")[0]
}

fun File.clearAndDelete(): Boolean {
    val allContents = this.listFiles()
    if (allContents != null) {
        for (file in allContents) {
            file.clearAndDelete()
        }
    }
    return this.delete()

}


fun File.getExtension(): String {
    val fileName = name
    val i: Int = fileName.lastIndexOf('.')
    return fileName.substring(i + 1)
}

object Files {
    fun nameWithoutTimestamp(name: String): String {
        return name.substring(name.indexOf("_") + 1)
    }
}