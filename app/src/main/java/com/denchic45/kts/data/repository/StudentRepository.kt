package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.UserDao
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.firestore.UserDoc
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.NetworkException
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.rxjava3.core.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class StudentRepository @Inject constructor(
    context: Context,
    override val networkService: NetworkService,
    private val userDao: UserDao,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val userMapper: UserMapper,
    private val firestore: FirebaseFirestore
) : Repository(context) {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val avatarRef: StorageReference = storage.reference.child("avatars")
    private val userRef: CollectionReference = firestore.collection("Users")
    private val groupRef: CollectionReference = firestore.collection("Groups")

    suspend fun add(student: User): Unit = withContext(dispatcher) {
        val batch = firestore.batch()
        val studentDoc = userMapper.domainToDoc(student)
        batch[userRef.document(student.uuid)] = studentDoc
        updateStudentFromGroup(studentDoc, student.groupUuid!!, batch)
        batch.commit().await()
    }

    suspend fun update(student: User): Unit = withContext(dispatcher) {
        val batch = firestore.batch()
        val studentDoc = userMapper.domainToDoc(student)
        val cacheStudent = userMapper.entityToDomain(userDao.getByUuidSync(student.uuid))
        if (changePersonalData(student, cacheStudent)) {
            batch[userRef.document(student.uuid)] = studentDoc
        }
        if (changeGroup(student, cacheStudent)) {
            deleteStudentFromGroup(studentDoc, cacheStudent.groupUuid!!, batch)
        }
        updateStudentFromGroup(studentDoc, student.groupUuid!!, batch)
        batch.commit().await()
    }

    private fun changePersonalData(student: User, cacheStudent: User): Boolean {
        return student.fullName != cacheStudent.fullName || student.gender != cacheStudent.gender ||
                student.firstName != cacheStudent.firstName ||
                student.phoneNum != cacheStudent.phoneNum ||
                student.email != cacheStudent.email ||
                student.photoUrl != cacheStudent.photoUrl || student.admin != cacheStudent.admin ||
                student.role != cacheStudent.role
    }

    private fun changeGroup(student: User, cacheStudent: User): Boolean {
        return student.groupUuid != cacheStudent.groupUuid
    }

    fun remove(student: User): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            val batch = firestore.batch()
            val studentDoc = userMapper.domainToDoc(student)
            batch.delete(userRef.document(student.uuid))
            deleteStudentFromGroup(studentDoc, student.groupUuid!!, batch)
            batch.commit()
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
            deleteAvatar(student.uuid)
        }
    }

    private fun deleteAvatar(uuid_user: String) {
        val reference = avatarRef.child(uuid_user)
        reference.delete()
            .addOnSuccessListener { Log.d("lol", "onSuccess: ") }
            .addOnFailureListener { e: Exception -> Log.d("lol", "onFailure: ", e) }
    }

    private fun updateStudentFromGroup(studentDoc: UserDoc, groupUuid: String, batch: WriteBatch) {
        val updateGroupMap: MutableMap<String, Any> = HashMap()
        updateGroupMap["students." + studentDoc.uuid] = studentDoc
        updateGroupMap["timestamp"] = FieldValue.serverTimestamp()
        batch.update(groupRef.document(groupUuid), updateGroupMap)
    }

    private fun deleteStudentFromGroup(studentDoc: UserDoc, groupUuid: String, batch: WriteBatch) {
        val updateGroupMap: MutableMap<String, Any> = HashMap()
        updateGroupMap["students." + studentDoc.uuid] = FieldValue.delete()
        updateGroupMap["timestamp"] = FieldValue.serverTimestamp()
        batch.update(groupRef.document(groupUuid), updateGroupMap)
    }

}