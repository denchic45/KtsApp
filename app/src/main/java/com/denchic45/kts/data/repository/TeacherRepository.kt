package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import com.denchic45.appVersion.AppVersionService
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.getDataFlow
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.UserDoc
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.utils.SearchKeysGenerator
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class TeacherRepository @Inject constructor(
    context: Context,
    private val userMapper: UserMapper,
    override val appVersionService: AppVersionService,
    override val networkService: NetworkService
) : Repository(context), FindByContainsNameRepository<User> {
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val usersRef: CollectionReference = firestore.collection("Users")
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val coursesRef: CollectionReference = firestore.collection("Courses")

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val avatarsRef: StorageReference = storage.reference.child("avatars")
    private var batch: WriteBatch? = null

    override fun findByContainsName(text: String): Flow<List<User>> {
        requireNetworkAvailable()
        return usersRef.whereArrayContains(
            "searchKeys",
            SearchKeysGenerator.formatInput(text)
        )
            .whereEqualTo("teacher", true)
            .getDataFlow { userMapper.docToDomain(it.toObjects(UserDoc::class.java)) }
    }

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
            findCoursesWithTeacherQuery(teacher.id),
            findGroupWithCuratorQuery(teacher.id)
        ).await()
        val coursesWithThisTeacherSnapshot = tasks[0].result as QuerySnapshot?
        val groupWithThisCuratorSnapshot = tasks[1].result as QuerySnapshot?
        if (!coursesWithThisTeacherSnapshot!!.isEmpty) {
            val coursesWithThisTeacher = coursesWithThisTeacherSnapshot.toObjects(
                CourseDoc::class.java
            )
            for (courseDoc in coursesWithThisTeacher) {
                val updateCourseMap: MutableMap<String, Any> = HashMap()
                updateCourseMap["teacher"] = teacherDoc
                updateCourseMap["timestamp"] = FieldValue.serverTimestamp()
                batch!!.update(coursesRef.document(courseDoc.id), updateCourseMap)
            }
        }
        if (!groupWithThisCuratorSnapshot!!.isEmpty) {
            val groupWithThisCurator =
                groupWithThisCuratorSnapshot.toObjects(GroupDoc::class.java)[0]
            val updateGroupMap: MutableMap<String, Any> = HashMap()
            updateGroupMap["curator"] = teacherDoc
            updateGroupMap["timestamp"] = FieldValue.serverTimestamp()
            batch!!.update(groupsRef.document(groupWithThisCurator.id), updateGroupMap)
        }
        batch!![usersRef.document(teacherDoc.id)] = teacherDoc
        batch!!.commit().await()
        return teacher
    }

    private fun findCoursesWithTeacherQuery(teacherId: String): Task<QuerySnapshot> {
        return coursesRef.whereEqualTo("teacher.id", teacherId).get()
    }

    private fun findGroupWithCuratorQuery(teacherId: String): Task<QuerySnapshot> {
        return groupsRef.whereEqualTo("curator.id", teacherId).get()
    }


    suspend fun remove(teacher: User) {
        requireAllowWriteData()
        batch = firestore.batch()
        batch!!.delete(usersRef.document(teacher.id))
        batch!!.commit()
            .await()
        deleteAvatar(teacher.id)
    }

    private fun deleteAvatar(userId: String) {
        val reference = avatarsRef.child(userId)
        reference.delete().addOnSuccessListener { aVoid: Void? -> Log.d("lol", "onSuccess: ") }
            .addOnFailureListener { e: Exception? -> Log.d("lol", "onFailure: ", e) }
    }

}