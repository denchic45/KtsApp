package com.denchic45.kts.data.remote.db

import kotlinx.coroutines.flow.Flow

expect class UserRemoteDataSource {

    fun observeById(id: String): Flow<Map<String, Any>?>

    fun findByContainsName(text: String): Flow<List<Map<String, Any>>>

    suspend fun findAndByEmail(email: String): Map<String, Any>
}