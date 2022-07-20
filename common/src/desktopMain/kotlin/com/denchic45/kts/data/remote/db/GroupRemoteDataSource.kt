package com.denchic45.kts.data.remote.db

import kotlinx.coroutines.flow.Flow

actual class GroupRemoteDataSource {
    actual fun observeById(id: String): Flow<Map<String, Any>?> {
        TODO("Not yet implemented")
    }

    actual fun findByContainsName(text: String): Flow<List<Map<String, Any>>> {
        TODO("Not yet implemented")
    }

    actual suspend fun remove(id: String) {
    }

}