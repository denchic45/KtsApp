package com.denchic45.kts.data.remote.db

import android.util.Log
import com.denchic45.kts.util.SearchKeysGenerator
import com.denchic45.kts.util.getQuerySnapshotFlow
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

actual class UserRemoteDataSource(
    val firestore: FirebaseFirestore
) {
    private val usersRef: CollectionReference = firestore.collection("Users")

    actual fun observeById(id: String): Flow<Map<String, Any>?> {
        return usersRef.whereEqualTo("id", id)
            .getQuerySnapshotFlow()
            .map { snapshot -> snapshot.documents[0].data }

    }

    actual fun findByContainsName(text: String): Flow<List<Map<String, Any>>> {
        return usersRef
            .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(text))
            .getQuerySnapshotFlow()
            .map { snapshot -> snapshot.documents.map { it.data!! } }
    }

    actual suspend fun findAndByEmail(email: String): Map<String, Any> {
        return usersRef.whereEqualTo("email", email)
            .get()
            .await().run {
                Log.d("lol", "A whereEqualTo awaited: ")
                if (isEmpty) {
                    throw  FirebaseAuthException(
                        "ERROR_USER_NOT_FOUND",
                        "Nothing user!"
                    )
                }
                documents[0].data!!
            }
    }


}