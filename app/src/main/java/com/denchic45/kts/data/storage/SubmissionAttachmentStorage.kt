package com.denchic45.kts.data.storage

import android.content.Context
import android.net.Uri
import com.denchic45.kts.data.DownloadByUrlApi
import com.denchic45.kts.data.model.domain.Attachment
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

class SubmissionAttachmentStorage @Inject constructor(
    context: Context,
    private val firebaseStorage: FirebaseStorage,
    private val retrofit: Retrofit
) {

    private val submissionAttachmentsRef = firebaseStorage.reference.child("submission_attachments")
    private val internalDir = context.applicationContext.filesDir
    private val submissionPath = File("${internalDir.path}/submissions")


    //TODO rewrite
    suspend fun addContentAttachments(
        parentId: String,
        attachments: List<Attachment>
    ): List<String> {
        return attachments.map {
            val timestamp =
                DateTimeFormatter.ofPattern("DD-MM-yyyy-HH-mm-ss").format(LocalDateTime.now())
            val filePath = submissionAttachmentsRef.child("$parentId/${timestamp}_${it.name}")
            filePath.putFile(Uri.fromFile(it.file)).await()
            filePath.downloadUrl.await().toString()
        }
    }

    suspend fun get(submissionId: String, studentId: String, urls: List<String>): List<Attachment> {
        return withContext(Dispatchers.IO) {
            if (urls.isEmpty())
                emptyList()
            else urls.map { url ->
                val timestamp = url.substring(url.lastIndexOf("/o/"), url.indexOf('_'))
                val attachmentPath = submissionPath.path + '/' + submissionId + '/' + studentId
                val itContentDir = File(attachmentPath)
                val listFiles = itContentDir.listFiles { _, name -> name.startsWith(timestamp) }
                if (listFiles.isNullOrEmpty()) {
                    val response = retrofit.create(DownloadByUrlApi::class.java).invoke(url)

                    val name = firebaseStorage.getReferenceFromUrl(url).name

                    File(attachmentPath).mkdirs()
                    val contentDir = File(attachmentPath, name)
                    contentDir.createNewFile()
                    val fileOutputStream = FileOutputStream(contentDir)
                    fileOutputStream.write(response.body()!!.bytes())
                    fileOutputStream.close()
                    Attachment(file = contentDir)
                } else {
                    Attachment(file = listFiles[0])
                }
            }
        }
    }

    //TODO rewrite
    suspend fun update(id: String, attachments: List<Attachment>): List<String> {
        val currentFiles = submissionAttachmentsRef.child(id).listAll().await().items
        val currentFileNames = currentFiles.map { it.name }.toSet()
        val updatedFileNames = attachments.map { it.name }.toSet()

        val added = attachments.filterNot { currentFileNames.contains(it.name) }
        val removed = currentFiles.filterNot { updatedFileNames.contains(it.name) }

        removed.forEach { it.delete().await() }
        val uploaded = addContentAttachments(id, added)

        return currentFiles.minus(removed.toSet())
            .map { it.downloadUrl.await().toString() } + uploaded
    }

    //TODO rewrite
    suspend fun deleteFilesByContentId(id: String) {
        submissionAttachmentsRef.child(id).listAll().await()
            .items.forEach { it.delete().await() }
    }

    //TODO rewrite
    fun deleteFromLocal(contentId: String) {
        val contentDir = File(submissionPath.path + '/' + contentId)
        contentDir.listFiles()?.forEach { it.delete() }
        contentDir.delete()
    }
}