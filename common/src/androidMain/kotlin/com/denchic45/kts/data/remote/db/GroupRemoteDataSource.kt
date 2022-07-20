package com.denchic45.kts.data.remote.db

import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

actual class GroupRemoteDataSource(
    val firestore: FirebaseFirestore
) {

    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val coursesRef: CollectionReference = firestore.collection("Courses")

    actual fun observeById(id: String): Flow<Map<String, Any>?> {
        TODO("Not yet implemented")
    }

    actual fun findByContainsName(text: String): Flow<List<Map<String, Any>>> {
        TODO("Not yet implemented")
    }

    actual suspend fun remove(id: String) {
    }

    actual suspend fun findById(groupId: String): Map<String, Any> {
        return groupDocReference(groupId)
            .get()
            .await()
            .data!!
    }

    actual suspend fun findCoursesByGroupId(groupId: String): List<String> {
        return coursesRef.whereArrayContains("groupIds", groupId)
            .get()
            .await()
            .map { courseDocSnapshot -> courseDocSnapshot.id }
    }


    private fun groupDocReference(groupId: String) = groupsRef.document(groupId)

}