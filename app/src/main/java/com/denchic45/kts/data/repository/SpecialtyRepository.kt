package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.denchic45.appVersion.AppVersionService
import com.denchic45.appVersion.GoogleAppVersionService
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.SpecialtyDao
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SpecialtyDoc
import com.denchic45.kts.data.model.mapper.SpecialtyMapper
import com.denchic45.kts.data.model.room.SpecialtyEntity
import com.denchic45.kts.di.modules.IoDispatcher
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class SpecialtyRepository @Inject constructor(
    override val context: Context,
    override val appVersionService: AppVersionService,
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
                        specialtyMapper.docToEntity(value.toObject(SpecialtyDoc::class.java)!!)
                    )
                }
            }
            .addOnFailureListener { e: Exception -> Log.d("lol", "onFailure: $e") }
        return Transformations.map(specialtyDao.get(id)) { entity: SpecialtyEntity ->
            specialtyMapper.entityToDomain(entity)
        }
    }

    fun findByTypedName(name: String): Flow<List<Specialty>> = callbackFlow {
        addListenerRegistration("name") {
            specialtyRef
                .whereArrayContains("searchKeys", name.lowercase(Locale.getDefault()))
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    val specialties = value!!.toObjects(
                        Specialty::class.java
                    )
                    launch {
                        specialtyDao.upsert(specialtyMapper.domainToEntity(specialties))
                        trySend(specialties)
                    }
                }
        }
        awaitClose { }
    }


    suspend fun add(specialty: Specialty) {
        requireInternetConnection()
        val data = specialtyMapper.domainToDoc(specialty)
        specialtyRef.document(specialty.id).set(data)
            .await()
    }

    suspend fun update(specialty: Specialty) {
        requireInternetConnection()
        val specialtyDoc = specialtyMapper.domainToDoc(specialty)
        val id = specialty.id
        groupsRef.whereEqualTo("specialty.id", id)
            .get()
            .await().apply {
                val batch = firestore.batch()
                if (!this.isEmpty) for (groupDoc in this.toObjects(
                    GroupDoc::class.java
                )) {
                    batch.update(
                        groupsRef.document(groupDoc.id), mapOf(
                            "specialty.$id" to specialtyDoc,
                            "timestamp" to FieldValue.serverTimestamp()
                        )
                    )
                }
                batch[specialtyRef.document(id)] = specialtyDoc
                batch.commit()
                    .await()
            }
    }

    suspend fun remove(specialty: Specialty) {
        requireInternetConnection()
        specialtyRef.document(specialty.id).delete()
            .await()
    }

}