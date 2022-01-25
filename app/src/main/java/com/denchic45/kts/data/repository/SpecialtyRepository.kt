package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource2
import com.denchic45.kts.data.dao.SpecialtyDao
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.SpecialtyMapper
import com.denchic45.kts.data.model.room.SpecialtyEntity
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.NetworkException
import com.google.firebase.firestore.*
import io.reactivex.rxjava3.core.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class SpecialtyRepository @Inject constructor(
    context: Context,
    private val coroutineScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val specialtyDao: SpecialtyDao,
    private val firestore: FirebaseFirestore,
    private val specialtyMapper: SpecialtyMapper,
    override val networkService: NetworkService
) : Repository(context) {

    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val specialtyRef: CollectionReference = firestore.collection("Specialties")

    fun find(id: String): LiveData<Specialty> {
        specialtyRef.document(id)
            .get()
            .addOnSuccessListener { value: DocumentSnapshot ->
                coroutineScope.launch(dispatcher) {
                    specialtyDao.upsert(
                        value.toObject(
                            SpecialtyEntity::class.java
                        )
                    )
                }
            }
            .addOnFailureListener { e: Exception -> Log.d("lol", "onFailure: $e") }
        return Transformations.map(specialtyDao.get(id)) { entity: SpecialtyEntity ->
            specialtyMapper.entityToDomain(entity)
        }
    }

    fun findByTypedName(name: String): Flow<Resource2<List<Specialty>>> = callbackFlow {
        addListenerRegistration("name") {
            specialtyRef
                .whereArrayContains("searchKeys", name.lowercase(Locale.getDefault()))
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    val specialties = value!!.toObjects(
                        Specialty::class.java
                    )
                    coroutineScope.launch(dispatcher) {
                        specialtyDao.upsert(specialtyMapper.domainToEntity(specialties))
                        trySend(Resource2.Success(specialties))
                    }
                }
        }
        awaitClose { }
    }


    fun add(specialty: Specialty): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            val data = specialtyMapper.domainToDoc(specialty)
            specialtyRef.document(specialty.id).set(data)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener(emitter::onError)
        }
    }

    fun update(specialty: Specialty): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            val specialtyDoc = specialtyMapper.domainToDoc(specialty)
            val id = specialty.id
            groupsRef.whereEqualTo("specialty.id", id)
                .get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    val batch = firestore.batch()
                    if (!queryDocumentSnapshots.isEmpty) for ((id1) in queryDocumentSnapshots.toObjects(
                        GroupDoc::class.java
                    )) {
                        batch.update(
                            groupsRef.document(id1), mapOf(
                                "specialty.$id" to specialtyDoc,
                                "timestamp" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                    batch[specialtyRef.document(id)] = specialtyDoc
                    batch.commit()
                        .addOnSuccessListener { emitter.onComplete() }
                        .addOnFailureListener(emitter::onError)
                }
        }
    }

    fun remove(specialty: Specialty): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            specialtyRef.document(specialty.id).delete()
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener(emitter::onError)
        }
    }

}