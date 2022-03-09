package com.denchic45.kts.data.repository

import android.net.Uri
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.CourseMapper
import com.denchic45.kts.data.model.mapper.SectionMapper
import com.denchic45.kts.data.model.mapper.SubjectMapper
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.data.model.room.SubjectEntity
import com.denchic45.kts.di.modules.IoDispatcher
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class SubjectRepository @Inject constructor(
    override val coroutineScope: CoroutineScope,
    override val networkService: NetworkService,
    override val firestore: FirebaseFirestore,
    override val courseMapper: CourseMapper,
    override val subjectMapper: SubjectMapper,
    override val userMapper: UserMapper,
    override val sectionMapper: SectionMapper,
    override val courseDao: CourseDao,
    override val groupCourseDao: GroupCourseDao,
    override val groupDao: GroupDao,
    override val sectionDao: SectionDao,
    override val subjectDao: SubjectDao,
    override val userDao: UserDao,
    @IoDispatcher override val dispatcher: CoroutineDispatcher
) : Repository(), SaveCourseOperation, RemoveCourseOperation {

    private val subjectsRef: CollectionReference = firestore.collection("Subjects")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    override val coursesRef: CollectionReference = firestore.collection("Courses")

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
        if (!queryDocumentSnapshots.isEmpty)
            for (groupDoc in queryDocumentSnapshots.toObjects(GroupDoc::class.java)) {
                batch.update(
                    groupsRef.document(groupDoc.id), mapOf(
                        "subjects.${groupDoc.id}" to subjectDoc,
                        "timestamp" to FieldValue.serverTimestamp()
                    )
                )
            }
        batch[subjectsRef.document(id)] = subjectDoc
        batch.commit().await()
    }

    private suspend fun isExistWithSameIconAndColor(subject: Subject) {
        val snapshot = subjectsRef
            .whereNotEqualTo("id", subject.id)
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
        coursesRef.whereEqualTo("subject.id", subject.id)
            .get()
            .await()
            .forEach {
                removeCourse(it.id)
            }

    }

    fun find(id: String): Flow<Subject?> {
        subjectsRef.document(id)
            .addSnapshotListener { value, error ->
                coroutineScope.launch(dispatcher) {
                    value?.let {
                        if (value.exists())
                            subjectDao.upsert(
                                value.toObject(SubjectEntity::class.java)!!
                            )
                    }
                }
            }

        return subjectDao.observe(id).map { it?.let { subjectMapper.entityToDomain(it) } }

    }

    fun findByTypedName(subjectName: String): Flow<List<Subject>> = callbackFlow {
        subjectsRef.whereArrayContains("searchKeys", subjectName.lowercase(Locale.getDefault()))
            .addSnapshotListener { value: QuerySnapshot?, _: FirebaseFirestoreException? ->
                val subjects = value!!.toObjects(
                    Subject::class.java
                )
                trySend(subjects)
                coroutineScope.launch(dispatcher) {
                    subjectDao.upsert(subjectMapper.domainToEntity(subjects))
                }
            }
        awaitClose { }
    }

    suspend fun findAllRefsOfSubjectIcons(): List<Uri> {
        return storage.getReference("subjects")
            .listAll()
            .await()
            .run {
                items.map { it.downloadUrl.await() }
            }
    }

    fun findByGroup(groupId: String): Flow<List<Subject>> {
        checkInternetConnection()
        coroutineScope.launch {
            coursesRef.whereArrayContains("groupIds", groupId)
                .get()
                .await().apply {
                    saveCourses(toObjects(CourseDoc::class.java))
                }
        }

        return subjectDao.observeByGroupId(groupId)
            .map(subjectMapper::entityToDomain)
    }

}