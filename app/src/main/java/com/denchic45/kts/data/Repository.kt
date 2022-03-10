package com.denchic45.kts.data

import com.denchic45.kts.data.Repository.Subscription
import com.denchic45.kts.utils.NetworkException
import com.google.firebase.firestore.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.reflect.full.memberProperties

abstract class Repository protected constructor() : CheckNetworkConnection {
    private val subscriptions: MutableMap<String, Subscription> = HashMap()


    protected fun addListenerRegistration(
        key: String,
        lambdaRegistration: () -> ListenerRegistration
    ) {
        val listenerRegistration = lambdaRegistration()
        if (subscriptions.containsKey(key)) {
            removeListener(key)
            subscriptions.replace(key) { listenerRegistration.remove() }
        } else {
            subscriptions[key] = Subscription { listenerRegistration.remove() }
        }
    }


    protected fun addListenerRegistration(key: String, subscription: Subscription) {
        if (subscriptions.containsKey(key)) {
            removeListener(key)
            subscriptions.replace(key, subscription)
        } else {
            subscriptions[key] = subscription
        }
    }

    protected fun hasListener(key: String): Boolean {
        return subscriptions.containsKey(key)
    }

    protected fun addListenerRegistrationIfNotExist(
        key: String,
        lambdaRegistration: () -> ListenerRegistration
    ) {
        if (!subscriptions.containsKey(key)) {
            val registration = lambdaRegistration()
            subscriptions[key] = Subscription { registration.remove() }
        }
    }


    protected fun addListenerRegistrationIfNotExist(key: String, registration: Subscription) {
        if (!subscriptions.containsKey(key)) subscriptions[key] = registration
    }

    fun removeListenerRegistration(key: String) {
        subscriptions[key]?.unsubscribe()
        subscriptions.remove(key)
    }

    open fun removeListeners() {
        subscriptions.values.forEach(Consumer { obj: Subscription -> obj.unsubscribe() })
    }

    private fun removeListener(key: String) {
        subscriptions[key]!!.unsubscribe()
    }

    fun interface Subscription {
        fun unsubscribe()
    }

    class Subscription2<T>(val lambdaValue: () -> T, private val lambdaUnsubscribe: Subscription) {
        var value: T? = null
        fun subscribe() {
            value = lambdaValue()
        }

        fun unsubscribe() {
            lambdaUnsubscribe.unsubscribe()
        }
    }

    fun timestampsIsNull(querySnapshot: QuerySnapshot): Boolean {
        return querySnapshot.documents.any { timestampIsNull(it) }
    }

    fun timestampIsNull(documentSnapshot: DocumentSnapshot): Boolean {
        return documentSnapshot.getTimestamp("timestamp") == null
    }

    fun timestampsNotNull(querySnapshot: QuerySnapshot): Boolean {
        return !timestampsIsNull(querySnapshot)
    }

    fun timestampNotNull(documentSnapshot: DocumentSnapshot): Boolean {
        return !timestampIsNull(documentSnapshot)
    }

    protected inline fun <reified T : Any> T.asMap(): Map<String, Any?> {
        val props = T::class.memberProperties.associateBy { it.name }
        return props.keys.associateWith { props[it]?.get(this) }
    }

    protected inline fun <reified T : Any> T.asMutableMap(): MutableMap<String, Any?> {
        return asMap().toMutableMap()
    }

}

interface CheckNetworkConnection {

    val networkService: NetworkService

    val isNetworkNotAvailable: Boolean
        get() = !networkService.isNetworkAvailable

    fun checkInternetConnection() {
        if (isNetworkNotAvailable) throw NetworkException()
    }
}

@ExperimentalCoroutinesApi
fun Query.getQuerySnapshotFlow(): Flow<QuerySnapshot?> {
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
fun <T> Query.getDataFlow(mapper: (QuerySnapshot?) -> T): Flow<T> {
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
