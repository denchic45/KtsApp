package com.denchic45.kts.data.storage

import android.content.Context
import android.net.Uri
import com.denchic45.kts.data.DownloadByUrlApi
import com.denchic45.kts.data.model.domain.Attachment
import com.denchic45.kts.utils.clearAndDelete
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
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
    private val submissionLocalPath = File("${internalDir.path}/submissions")

    suspend fun addSubmissionAttachments(
        contentId: String,
        studentId: String,
        attachments: List<Attachment>
    ): List<String> {
        return attachments.map { attachment ->
            try {
                getSubmissionReference(
                    contentId,
                    studentId
                ).child(attachment.name).downloadUrl.await().toString()
            } catch (exception: Exception) {
                if (exception is StorageException &&
                    exception.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND
                ) {
                    val timestamp =
                        DateTimeFormatter.ofPattern("DD-MM-yyyy-HH-mm-ss")
                            .format(LocalDateTime.now())
                    val filePath =
                        submissionAttachmentsRef.child("$contentId/$studentId/${timestamp}_${attachment.name}")
                    filePath.putFile(Uri.fromFile(attachment.file)).await()
                    filePath.downloadUrl.await().toString()
                } else {
                    exception.printStackTrace()
                    throw exception
                }
            }
        }
    }

    suspend fun get(contentId: String, studentId: String, urls: List<String>): List<Attachment> {
        return withContext(Dispatchers.IO) {
            if (urls.isEmpty()) emptyList()
            else {
                val attachmentPath = getSubmissionLocalPath(contentId, studentId)
                val itContentDir = File(attachmentPath)
                val listFiles = itContentDir.listFiles()

                urls.map { url ->
                    val name = firebaseStorage.getReferenceFromUrl(url).name
                    listFiles?.firstOrNull { it.name == name }?.let {
                        Attachment(file = it)
                    } ?: run {
                        val response = retrofit.create(DownloadByUrlApi::class.java).invoke(url)

                        File(attachmentPath).mkdirs()
                        val contentDir = File(attachmentPath, name)
                        contentDir.createNewFile()
                        val fileOutputStream = FileOutputStream(contentDir)
                        fileOutputStream.write(response.body()!!.bytes())
                        fileOutputStream.close()
                        Attachment(file = contentDir)
                    }

                }
            }
        }
    }

    suspend fun update(
        contentId: String,
        studentId: String,
        attachments: List<Attachment>
    ): List<String> {
        val currentFiles = getSubmissionReference(contentId, studentId).listAll().await().items

        val currentFileNames = currentFiles.map { it.name }.toSet()
        val updatedFileNames = attachments.map { it.name }.toSet()

        val added = attachments.filterNot { currentFileNames.contains(it.name) }
        val removed = currentFiles.filterNot { updatedFileNames.contains(it.name) }

        removed.forEach { it.delete().await() }
        val uploaded = addSubmissionAttachments(contentId, studentId, added)

        return currentFiles.minus(removed.toSet())
            .map { it.downloadUrl.await().toString() } + uploaded
    }

    suspend fun deleteFilesByContentId(contentId: String) {
        getSubmissionsReference(contentId).deleteFolder()
    }

    suspend fun StorageReference.deleteFolder() {
        listAll().await().apply {
            items.forEach {
                it.delete().await()
            }
            prefixes.forEach { it.deleteFolder() }
        }
    }

    fun deleteFromLocal(contentId: String) {
        val contentDir = File(getSubmissionsLocalPath(contentId))
        contentDir.clearAndDelete()
    }

    fun getNames(urls: List<String>): List<String> {
        return urls.map { url ->
            firebaseStorage.getReferenceFromUrl(url).name
        }
    }

    private fun getSubmissionReference(contentId: String, studentId: String): StorageReference {
        return submissionAttachmentsRef.child(contentId).child(studentId)
    }

    private fun getSubmissionsReference(contentId: String): StorageReference {
        return submissionAttachmentsRef.child(contentId)
    }

    private fun getSubmissionLocalPath(contentId: String, studentId: String): String {
        return submissionLocalPath.path + '/' + contentId + '/' + studentId
    }

    private fun getSubmissionsLocalPath(contentId: String): String {
        return submissionLocalPath.path + '/' + contentId
    }
}