package com.denchic45.kts.data.remote.storage

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.tasks.await

actual class UserRemoteStorage(storage: FirebaseStorage) {

    private val avatarStorage: StorageReference = storage.reference.child("avatars")

    actual suspend fun uploadAvatar(bytes: ByteArray, userId: String): String {
        val reference = avatarStorage.child(userId)
        reference.putBytes(bytes).await()
        return reference.downloadUrl.await().toString()
    }

}