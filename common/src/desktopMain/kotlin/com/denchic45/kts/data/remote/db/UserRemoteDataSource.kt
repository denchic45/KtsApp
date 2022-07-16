package com.denchic45.kts.data.remote.db

import kotlinx.coroutines.flow.Flow

actual class UserRemoteDataSource {
    actual fun findByContainsName(text: String): Flow<List<Map<String, Any>>> {
        TODO("Not yet implemented")
    }

    actual fun observeById(id: String): Flow<Map<String, Any>?> {
        TODO("Not yet implemented")
    }

    actual suspend fun findAndByEmail(email: String): Map<String, Any> {
        TODO("Not yet implemented")
    }
}