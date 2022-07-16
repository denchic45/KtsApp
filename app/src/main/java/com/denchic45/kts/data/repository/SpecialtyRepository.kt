package com.denchic45.kts.data.repository

import com.denchic45.kts.data.dao.SpecialtyDao
import com.denchic45.kts.data.model.domain.Specialty
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SpecialtyDoc
import com.denchic45.kts.data.model.mapper.SpecialtyMapper
import com.denchic45.kts.data.service.AppVersionService
import com.denchic45.kts.data.service.NetworkService
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.util.SearchKeysGenerator
import com.denchic45.kts.util.getDataFlow
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SpecialtyRepository @Inject constructor(
    override val appVersionService: AppVersionService,
    private val coroutineScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val specialtyDao: SpecialtyDao,
    private val firestore: FirebaseFirestore,
    private val specialtyMapper: SpecialtyMapper,
    override val networkService: NetworkService
) : Repository(), FindByContainsNameRepository<Specialty> {

    override fun findByContainsName(text: String): Flow<List<Specialty>> {
        return specialtyRef
            .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(text))
            .getDataFlow { it.toObjects(SpecialtyDoc::class.java) }
            .map { docs ->
                specialtyDao.upsert(specialtyMapper.docToEntity(docs))
                specialtyMapper.docToDomain(docs)
            }
    }

    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val specialtyRef: CollectionReference = firestore.collection("Specialties")

    fun observe(id: String): Flow<Specialty?> {
        coroutineScope.launch {
            specialtyRef.document(id)
                .get()
                .await().apply {
                    coroutineScope.launch(dispatcher) {
                        specialtyDao.upsert(
                            specialtyMapper.docToEntity(toObject(SpecialtyDoc::class.java)!!)
                        )
                    }
                }
        }
        return specialtyDao.observe(id).map { entity ->
            entity?.let { specialtyMapper.entityToDomain(entity) }
        }
    }

    suspend fun add(specialty: Specialty) {
        requireAllowWriteData()
        val data = specialtyMapper.domainToDoc(specialty)
        specialtyRef.document(specialty.id).set(data)
            .await()

    }

    suspend fun update(specialty: Specialty) {
        requireAllowWriteData()
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
        requireAllowWriteData()
        specialtyRef.document(specialty.id).delete()
            .await()
    }

}