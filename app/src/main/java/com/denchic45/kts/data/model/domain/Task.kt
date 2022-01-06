package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.utils.Files
import java.io.File
import java.util.*
import android.provider.MediaStore

import android.media.ThumbnailUtils

import android.graphics.Bitmap
import android.os.Build
import android.os.CancellationSignal
import android.util.Size
import android.webkit.MimeTypeMap
import com.denchic45.kts.utils.UUIDS
import com.denchic45.kts.utils.getExtension


data class Task(
    override var uuid: String,
    val courseId: String,
    val sectionId: String,
    val name: String,
    val content: String,
    val attachments: List<Attachment>,
    val completionDate: Date,
    val createdDate: Date,
    val updatedDate: Date,
    val completed: Boolean,
) : DomainModel() {

    private constructor() : this("", "", "", "", "", emptyList(), Date(0), Date(0), Date(), false)

    companion object {
        fun createEmpty() = Task()
    }
}

data class Attachment(
    override var uuid: String = UUIDS.createShort(),
    val file: File
    ) : DomainModel() {

    val name: String = file.name

    val extension: String = file.getExtension()

}