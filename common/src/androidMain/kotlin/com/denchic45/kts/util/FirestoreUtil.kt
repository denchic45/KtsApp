package com.denchic45.kts.util

import android.util.Log
import com.google.firebase.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlin.reflect.full.memberProperties


@ExperimentalCoroutinesApi
fun Query.getLocalDataAndObserveRemote(): Flow<QuerySnapshot?> {
    return callbackFlow {
        val listenerRegistration =
            addSnapshotListener { querySnapshot, firestoreException ->
                if (firestoreException != null) {
                    cancel(
                        message = "Error on fetching query",
                        cause = firestoreException
                    )
                    return@addSnapshotListener
                }
                launch {
                    send(querySnapshot)
                }
            }
        awaitClose {
            listenerRegistration.remove()
        }
    }
}

@ExperimentalCoroutinesApi
fun <T> Flow<T>.withSnapshotListener(
    query: Query,
    onQuerySnapshot: (QuerySnapshot) -> Unit,
    onErrorSnapshot: (Throwable) -> Unit = {}
): Flow<T> {
    val listenerRegistration = query.addSnapshotListener { value, error ->
        if (error != null) {
            onErrorSnapshot(error)
        } else if (value != null) {
            onQuerySnapshot(value)
        }
    }
    return onCompletion {
        Log.d("lol", "withSnapshotListener: $query")
        listenerRegistration.remove()
    }
}

@ExperimentalCoroutinesApi
fun <T> Flow<T>.withSnapshotListener(
    documentReference: DocumentReference,
    onDocumentSnapshot: (DocumentSnapshot) -> Unit,
    onErrorSnapshot: (Throwable) -> Unit = {}
): Flow<T> {
    val listenerRegistration = documentReference.addSnapshotListener { value, error ->
        if (error != null) {
            onErrorSnapshot(error)
        } else if (value != null) {
            onDocumentSnapshot(value)
        }
    }
    return this.onCompletion {
        listenerRegistration.remove()
    }
}

@ExperimentalCoroutinesApi
fun Query.getQuerySnapshotFlow(): Flow<QuerySnapshot> {
    return callbackFlow {
        val listenerRegistration =
            addSnapshotListener { querySnapshot, firestoreException ->
                if (firestoreException != null) {
                    cancel(
                        message = "Error on fetching query",
                        cause = firestoreException
                    )
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    launch {
                        send(querySnapshot)
                    }
                }
            }
        awaitClose {
            Log.d("lol", "getQuerySnapshotFlow awaitClose: ")
            listenerRegistration.remove()
        }
    }
}

@ExperimentalCoroutinesApi
fun <T> Query.getDataFlow(mapper: (QuerySnapshot) -> T): Flow<T> {
    return getQuerySnapshotFlow()
        .map {
            return@map mapper(it)
        }
}

fun CollectionReference.deleteCollection(batchSize: Int) {
    try {
        // Retrieve a small batch of documents to avoid out-of-memory errors/
        var deleted = 0
        this.limit(batchSize.toLong())
            .get()
            .addOnCompleteListener {
                for (document in it.result.documents) {
                    document.reference.delete()
                    ++deleted
                }
                if (deleted >= batchSize) {
                    // retrieve and delete another batch
                    deleteCollection(batchSize)
                }
            }
    } catch (e: Exception) {
        System.err.println("Error deleting collection : " + e.message)
    }
}

fun QuerySnapshot.timestampsIsNull(): Boolean {
    return documents.any { it.timestampIsNull() }
}

fun DocumentSnapshot.timestampIsNull(): Boolean {
    return getTimestamp("timestamp") == null
}

fun QuerySnapshot.timestampsNotNull(): Boolean {
    return !timestampsIsNull()
}

fun DocumentSnapshot.timestampNotNull(): Boolean {
    return !timestampIsNull()
}

 inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
    val props = T::class.memberProperties.associateBy { it.name }
    return props.keys.associateWith { props[it]?.get(this) }
}

 inline fun <reified T : Any> T.asMutableMap(): MutableMap<String, Any?> {
    return asMap().toMutableMap()
}