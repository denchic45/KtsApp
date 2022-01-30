package com.denchic45.kts.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.dao.SubjectDao
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.SubjectMapper
import com.denchic45.kts.data.model.room.SubjectEntity
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.NetworkException
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.core.SingleEmitter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.stream.Collectors
import javax.inject.Inject

class SubjectRepository @Inject constructor(
    context: Context,
    private val subjectDao: SubjectDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    override val networkService: NetworkService,
    private val firestore: FirebaseFirestore,
    private val subjectMapper: SubjectMapper
) : Repository(context) {

    private val subjectsRef: CollectionReference = firestore.collection("Subjects")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    fun findSpecialSubjects(): Single<List<Subject>> {
        return Single.create { emitter: SingleEmitter<List<Subject>> ->
            subjectsRef.whereEqualTo("special", true)
                .get()
                .addOnSuccessListener { queryDocumentSnapshots: QuerySnapshot ->
                    val list = queryDocumentSnapshots.toObjects(SubjectEntity::class.java)
                    coroutineScope.launch(dispatcher) {
                        subjectDao.insert(list)
                    }
                    emitter.onSuccess(
                        queryDocumentSnapshots.toObjects(
                            Subject::class.java
                        )
                    )
                }
                .addOnFailureListener(emitter::onError)
        }
    }

    suspend fun add(subject: Subject) {
        checkInternetConnection()
        isExistWithSameIconAndColor(subject)
        subjectsRef.document(subject.id)
            .set(subjectMapper.domainToDoc(subject))
            .await()
    }

    suspend fun update(subject: Subject) {
        checkInternetConnection()
        isExistWithSameIconAndColor(subject)
        val subjectDoc = subjectMapper.domainToDoc(subject)
        val id = subject.id
        val queryDocumentSnapshots = groupsRef.whereEqualTo("subjects.$id.id", id)
            .get()
            .await()
        val batch = firestore.batch()
        if (!queryDocumentSnapshots.isEmpty) for ((id) in queryDocumentSnapshots.toObjects(
            GroupDoc::class.java
        )) {
            batch.update(
                groupsRef.document(id), mapOf(
                    "subjects.$id" to subjectDoc,
                    "timestamp" to FieldValue.serverTimestamp()
                )
            )
        }
        batch[subjectsRef.document(id)] = subjectDoc
        batch.commit().await()
    }

    private suspend fun isExistWithSameIconAndColor(subject: Subject) {
        val snapshot = subjectsRef
            .whereEqualTo("iconUrl", subject.iconUrl)
            .whereEqualTo("colorName", subject.colorName)
            .get()
            .await()
        if (!snapshot.isEmpty) {
            throw SameSubjectIconException()
        }
    }

    suspend fun remove(subject: Subject) {
        checkInternetConnection()
        subjectsRef.document(subject.id).delete().await()
    }

    fun find(id: String): LiveData<Subject> {
        subjectsRef.document(id)
            .get()
            .addOnSuccessListener { value: DocumentSnapshot ->
                coroutineScope.launch(dispatcher) {
                    subjectDao.upsert(
                        value.toObject(SubjectEntity::class.java)!!
                    )
                }
            }
            .addOnFailureListener { Log.d("lol", "onFailure: ") }
        return Transformations.map(subjectDao.get(id)) { entity: SubjectEntity ->
            subjectMapper.entityToDomain(
                entity
            )
        }
    }

    fun findByTypedName(subjectName: String): Flow<Resource<List<Subject>>> = callbackFlow {
        subjectsRef.whereArrayContains("searchKeys", subjectName.lowercase(Locale.getDefault()))
            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                val subjects = value!!.toObjects(
                    Subject::class.java
                )
                trySend(Resource.Success(subjects))
                coroutineScope.launch(dispatcher) {
                    subjectDao.upsert(subjectMapper.domainToEntity(subjects))
                }
            }
        awaitClose { }
    }


    fun findLazy(subjectId: String): Subject {
        if (subjectDao.getSync(subjectId) == null) {
            subjectsRef.whereEqualTo("id", subjectId)
                .get()
                .addOnSuccessListener { snapshot: QuerySnapshot ->
                    coroutineScope.launch(dispatcher) {
                        subjectDao.insert(
                            snapshot.documents[0].toObject(SubjectEntity::class.java)!!
                        )
                    }
                }
                .addOnFailureListener { e: Exception? -> Log.d("lol", "onFailure: ", e) }
        }
        return subjectMapper.entityToDomain(subjectDao.getSync(subjectId))
    }

    fun findAllRefsOfSubjectIcons(): Single<List<Uri>> {
        return Single.create { emitter: SingleEmitter<List<Uri>> ->
            val singles: MutableList<Single<Uri>> = ArrayList()
            storage.getReference("subjects")
                .listAll()
                .addOnSuccessListener { listResult: ListResult ->
                    for (ref in listResult.items) {
                        singles.add(Single.create { singleEmitter: SingleEmitter<Uri> ->
                            ref.downloadUrl
                                .addOnSuccessListener { t: Uri -> singleEmitter.onSuccess(t) }
                        })
                    }
                    Single.zip(singles) { objects: Array<Any> ->
                        Arrays.stream(objects)
                            .map { o: Any? -> o as Uri }
                            .collect(Collectors.toList())
                    }
                        .subscribe { t: List<Uri> -> emitter.onSuccess(t) }
                }.addOnFailureListener { e: Exception ->
                    Log.d("lol", "onFailure: ", e)
                    Log.d("lol", "FAIL: ")
                }
        }
    }

    fun findByGroup(groupId: String): LiveData<Resource<List<Subject>>> {
        return if (!networkService.isNetworkAvailable) {
            MutableLiveData(Resource.Error(NetworkException()))
        } else Transformations.map(subjectDao.getByGroupId(groupId)) { input: List<SubjectEntity?> ->
            Resource.Success(
                subjectMapper.entityToDomain(input)
            )
        }
        //todo дописать!
//        if (!groupDao.isExistSync(groupId)) {
//            groupsRef.document(groupId)
//                    .get()
//                    .addOnSuccessListener(snapshot -> {
//                        GroupDoc groupDoc = snapshot.toObject(GroupDoc.class);
//                        subjectDao.upsert(subjectMapper.docToEntity(new ArrayList<>(groupDoc.getSubjects().values())));
//                    });
//        }
    }

}