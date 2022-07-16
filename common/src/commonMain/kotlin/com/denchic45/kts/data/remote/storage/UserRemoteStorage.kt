package com.denchic45.kts.data.remote.storage

expect class UserRemoteStorage {

    suspend  fun uploadAvatar(bytes: ByteArray, userId: String):String
}