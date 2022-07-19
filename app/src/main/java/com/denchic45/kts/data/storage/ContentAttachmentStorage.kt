package com.denchic45.kts.data.storage

import android.content.Context
import android.net.Uri
import com.denchic45.kts.data.DownloadByUrlApi
import com.denchic45.kts.domain.model.Attachment
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ContentAttachmentStorage @Inject constructor(
    context: Context,
    private val firebaseStorage: FirebaseStorage,
    private val retrofit: Retrofit
) {

    private val contentAttachmentsRef = firebaseStorage.reference.child("content_attachments")
    private val internalDir = context.applicationContext.filesDir
    private val contentPath = File("${internalDir.path}/contents")

    suspend fun addContentAttachments(
        parentId: String,
        attachments: List<Attachment>
    ): List<String> {
        return attachments.map {
            val timestamp =
                DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss").format(LocalDateTime.now())
            val filePath = contentAttachmentsRef.child("$parentId/${timestamp}_${it.name}")
            filePath.putFile(Uri.fromFile(it.file)).await()
            filePath.downloadUrl.await().toString()
        }
    }

    suspend fun get(contentId: String, urls: List<String>): List<Attachment> {
        return withContext(Dispatchers.IO) {
            if (urls.isEmpty())
                emptyList()
            else urls.map { url ->
                val timestamp = url.substring(url.lastIndexOf("/o/"), url.indexOf('_'))
                val attachmentPath = contentPath.path + '/' + contentId
                val itContentDir = File(attachmentPath)
                val listFiles = itContentDir.listFiles { _, name -> name.startsWith(timestamp) }
                if (listFiles.isNullOrEmpty()) {
                    val invoke = retrofit.create(DownloadByUrlApi::class.java)
                        .invoke(url)

                    val name = firebaseStorage.getReferenceFromUrl(url).name

                    File(attachmentPath).mkdirs()
                    val contentDir = File(attachmentPath, name)
                    contentDir.createNewFile()
                    val fileOutputStream = FileOutputStream(contentDir)
                    fileOutputStream.write(invoke.body()!!.bytes())
                    fileOutputStream.close()
                    Attachment(file = contentDir)
                } else {
                    Attachment(file = listFiles[0])
                }
            }
        }
    }

    suspend fun update(id: String, attachments: List<Attachment>): List<String> {
        val currentFiles = contentAttachmentsRef.child(id).listAll().await().items
        val currentFileNames = currentFiles.map { it.name }.toSet()
        val updatedFileNames = attachments.map { it.name }.toSet()

        val added = attachments.filterNot { currentFileNames.contains(it.name) }
        val removed = currentFiles.filterNot { updatedFileNames.contains(it.name) }

        removed.forEach { it.delete().await() }
        val uploaded = addContentAttachments(id, added)

        return currentFiles.minus(removed.toSet())
            .map { it.downloadUrl.await().toString() } + uploaded
    }

    suspend fun deleteFilesByContentId(id: String) {
        contentAttachmentsRef.child(id).listAll().await()
            .items.forEach { it.delete().await() }
    }

    fun deleteFromLocal(contentId: String) {
        val contentDir = File(contentPath.path + '/' + contentId)
        contentDir.listFiles()?.forEach { it.delete() }
        contentDir.delete()
    }


}

