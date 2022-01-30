package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.SearchKeysGenerator
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.rxjava3.core.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class TeacherRepository @Inject constructor(
    context: Context,
    private val userMapper: UserMapper
) : Repository(context) {

    private val usersRef: CollectionReference
    private val groupsRef: CollectionReference

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val storage: FirebaseStorage
    private val avatarsRef: StorageReference
    override val networkService: NetworkService
    private var batch: WriteBatch? = null

    suspend fun add(teacher: User): User {
        batch = firestore.batch()
        val teacherDoc = userMapper.domainToDoc(teacher)
        batch!![usersRef.document(teacherDoc.id)] = teacherDoc
        batch!!.commit().await()
        return teacher
    }

    suspend fun update(teacher: User): User {
        batch = firestore.batch()
        val teacherDoc = userMapper.domainToDoc(teacher)
        val tasks = Tasks.whenAllComplete(
            findGroupsWithTeacherQuery(teacher.id),
            findGroupWithCuratorQuery(teacher.id)
        ).await()
        val groupsWithThisTeacherSnapshot = tasks[0].result as QuerySnapshot?
        val groupWithThisCuratorSnapshot = tasks[1].result as QuerySnapshot?
        if (!groupsWithThisTeacherSnapshot!!.isEmpty) {
            val groupsWithThisTeacher = groupsWithThisTeacherSnapshot.toObjects(
                GroupDoc::class.java
            )
            for ((id) in groupsWithThisTeacher) {
                val updateGroupMap: MutableMap<String, Any> = HashMap()
                updateGroupMap["teachers." + teacher.id] = teacherDoc
                updateGroupMap["timestamp"] = FieldValue.serverTimestamp()
                batch!!.update(groupsRef.document(id), updateGroupMap)
            }
        }
        if (!groupWithThisCuratorSnapshot!!.isEmpty) {
            val (id) = groupWithThisCuratorSnapshot.toObjects(
                GroupDoc::class.java
            )[0]
            val updateGroupMap: MutableMap<String, Any> = HashMap()
            updateGroupMap["curator"] = teacherDoc
            updateGroupMap["timestamp"] = FieldValue.serverTimestamp()
            batch!!.update(groupsRef.document(id), updateGroupMap)
        }
        batch!![usersRef.document(teacherDoc.id)] = teacherDoc
        batch!!.commit().await()
        return teacher
    }

    private fun findGroupsWithTeacherQuery(teacherId: String): Task<QuerySnapshot> {
        return groupsRef.orderBy("teachers.$teacherId").get()
    }

    private fun findGroupWithCuratorQuery(teacherId: String): Task<QuerySnapshot> {
        return groupsRef.whereEqualTo("curator.id", teacherId).get()
    }

    fun findByTypedName(teacherName: String): Flow<Resource<List<User>>> = callbackFlow {
        if (!networkService.isNetworkAvailable) {
            trySend(Resource.Error(NetworkException()))
            return@callbackFlow
        }
        usersRef.whereArrayContains(
            "searchKeys",
            SearchKeysGenerator.formatInput(teacherName)
        )
            .whereEqualTo("teacher", true)
            .get()
            .addOnSuccessListener { snapshots: QuerySnapshot ->
                trySend(
                    Resource.Success(snapshots.toObjects(User::class.java))
                )
            }
            .addOnFailureListener(this::close)
        awaitClose { }
    }


    fun remove(teacher: User): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            batch = firestore.batch()
            batch!!.delete(usersRef.document(teacher.id))
            batch!!.commit()
                .addOnSuccessListener { command: Void? -> emitter.onComplete() }
                .addOnFailureListener { t: Exception? -> emitter.onError(t) }
            deleteAvatar(teacher.id)
        }
    }

    private fun deleteAvatar(userId: String) {
        val reference = avatarsRef.child(userId)
        reference.delete().addOnSuccessListener { aVoid: Void? -> Log.d("lol", "onSuccess: ") }
            .addOnFailureListener { e: Exception? -> Log.d("lol", "onFailure: ", e) }
    }

    init {
        usersRef = firestore.collection("Users")
        groupsRef = firestore.collection("Groups")
        storage = FirebaseStorage.getInstance()
        avatarsRef = storage.reference.child("avatars")
        networkService = NetworkService(context)
    }
}