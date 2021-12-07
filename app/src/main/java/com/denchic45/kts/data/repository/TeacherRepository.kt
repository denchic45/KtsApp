package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource2
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
        batch!![usersRef.document(teacherDoc.uuid)] = teacherDoc
        batch!!.commit().await()
        return teacher
    }

    suspend fun update(teacher: User): User {
        batch = firestore.batch()
        val teacherDoc = userMapper.domainToDoc(teacher)
        val tasks = Tasks.whenAllComplete(
            findGroupsWithTeacherQuery(teacher.uuid),
            findGroupWithCuratorQuery(teacher.uuid)
        ).await()
        val groupsWithThisTeacherSnapshot = tasks[0].result as QuerySnapshot?
        val groupWithThisCuratorSnapshot = tasks[1].result as QuerySnapshot?
        if (!groupsWithThisTeacherSnapshot!!.isEmpty) {
            val groupsWithThisTeacher = groupsWithThisTeacherSnapshot.toObjects(
                GroupDoc::class.java
            )
            for ((uuid) in groupsWithThisTeacher) {
                val updateGroupMap: MutableMap<String, Any> = HashMap()
                updateGroupMap["teachers." + teacher.uuid] = teacherDoc
                updateGroupMap["timestamp"] = FieldValue.serverTimestamp()
                batch!!.update(groupsRef.document(uuid), updateGroupMap)
            }
        }
        if (!groupWithThisCuratorSnapshot!!.isEmpty) {
            val (uuid) = groupWithThisCuratorSnapshot.toObjects(
                GroupDoc::class.java
            )[0]
            val updateGroupMap: MutableMap<String, Any> = HashMap()
            updateGroupMap["curator"] = teacherDoc
            updateGroupMap["timestamp"] = FieldValue.serverTimestamp()
            batch!!.update(groupsRef.document(uuid), updateGroupMap)
        }
        batch!![usersRef.document(teacherDoc.uuid)] = teacherDoc
        batch!!.commit().await()
        return teacher
    }

    fun findGroupsWithTeacherQuery(teacherUuid: String): Task<QuerySnapshot> {
        return groupsRef.orderBy("teachers.$teacherUuid").get()
    }

    fun findGroupWithCuratorQuery(teacherUuid: String?): Task<QuerySnapshot> {
        return groupsRef.whereEqualTo("curator.uuid", teacherUuid).get()
    }

    fun findByTypedName(teacherName: String): Flow<Resource2<List<User>>> = callbackFlow {
        if (!networkService.isNetworkAvailable) {
            trySend(Resource2.Error(NetworkException()))
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
                    Resource2.Success(snapshots.toObjects(User::class.java))
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
            batch!!.delete(usersRef.document(teacher.uuid))
            batch!!.commit()
                .addOnSuccessListener { command: Void? -> emitter.onComplete() }
                .addOnFailureListener { t: Exception? -> emitter.onError(t) }
            deleteAvatar(teacher.uuid)
        }
    }

    private fun deleteAvatar(uuid_user: String) {
        val reference = avatarsRef.child(uuid_user)
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