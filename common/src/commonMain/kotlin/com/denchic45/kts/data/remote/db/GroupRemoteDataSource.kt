package com.denchic45.kts.data.remote.db

import kotlinx.coroutines.flow.Flow

expect class GroupRemoteDataSource {

    fun observeById(id: String): Flow<Map<String, Any>?>

    fun findByContainsName(text: String): Flow<List<Map<String, Any>>>

    suspend fun remove(id: String)

   suspend fun findById(groupId: String): Map<String, Any>
    suspend fun findCoursesByGroupId(groupId: String): List<String>
}