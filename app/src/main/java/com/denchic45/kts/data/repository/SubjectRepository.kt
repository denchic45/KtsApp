package com.denchic45.kts.data.repository

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.denchic45.appVersion.AppVersionService
import com.denchic45.kts.data.database.DataBase
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.getDataFlow
import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SubjectDoc
import com.denchic45.kts.data.model.mapper.CourseMapper
import com.denchic45.kts.data.model.mapper.SectionMapper
import com.denchic45.kts.data.model.mapper.SubjectMapper
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.data.model.room.SubjectEntity
import com.denchic45.kts.di.modules.IoDispatcher
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class SubjectRepository @Inject constructor(
    override val context: Context,
    override val appVersionService: AppVersionService,
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
    private val dataBase: DataBase,
    @IoDispatcher override val dispatcher: CoroutineDispatcher,
) : Repository(context), SaveCourseRepository, RemoveCourseOperation,
    FindByContainsNameRepository<Subject> {

    private val subjectsRef: CollectionReference = firestore.collection("Subjects")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    override val groupsRef: CollectionReference = firestore.collection("Groups")
    override val coursesRef: CollectionReference = firestore.collection("Courses")

    suspend fun add(subject: Subject) {
        requireAllowWriteData()
        isExistWithSameIconAndColor(subject)
        subjectsRef.document(subject.id)
            .set(subjectMapper.domainToDoc(subject))
            .await()
    }

    suspend fun update(subject: Subject) {
        requireAllowWriteData()
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
        requireAllowWriteData()
        subjectsRef.document(subject.id).delete().await()
        coursesRef.whereEqualTo("subject.id", subject.id)
            .get()
            .await()
            .forEach {
                val courseDoc = it.toObject(CourseDoc::class.java)
                removeCourse(courseDoc.id, courseDoc.groupIds)
            }

    }

    fun find(id: String): Flow<Subject?> {
        subjectsRef.document(id)
            .addSnapshotListener { value, error ->
                coroutineScope.launch(dispatcher) {
                    value?.let {
                        if (value.exists())
                            saveSubject(subjectMapper.docToEntity(value.toObject(SubjectDoc::class.java)!!))
                    }
                }
            }

        return subjectDao.observe(id).map { it?.let { subjectMapper.entityToDomain(it) } }

    }

    private suspend fun saveSubject(subjectEntity: SubjectEntity) {
        subjectDao.upsert(subjectEntity)
    }

//    fun findByTypedName(subjectName: String): Flow<List<Subject>> = callbackFlow {
//        val addSnapshotListener =
//            subjectsRef.whereArrayContains("searchKeys", subjectName.lowercase(Locale.getDefault()))
//                .addSnapshotListener { value: QuerySnapshot?, _ ->
//                    val subjects = value!!.toObjects(Subject::class.java)
//                    trySend(subjects)
//                    launch(dispatcher) {
//                        subjectDao.upsert(subjectMapper.domainToEntity(subjects))
//                    }
//                }
//        awaitClose { addSnapshotListener.remove() }
//    }

    override fun findByContainsName(text: String): Flow<List<Subject>> {
        return subjectsRef.whereArrayContains("searchKeys", text.lowercase(Locale.getDefault()))
            .getDataFlow { it.toObjects(SubjectDoc::class.java) }
            .map { subjectDocs ->
                coroutineScope.launch {
                    subjectDao.upsert(subjectMapper.docToEntity(subjectDocs))
                }
                subjectMapper.docToDomain(subjectDocs)
            }
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
        coroutineScope.launch {
            requireAllowWriteData()
            coursesRef.whereArrayContains("groupIds", groupId)
                .get()
                .await().apply {
                    dataBase.withTransaction {
                        saveCourses(toObjects(CourseDoc::class.java))
                    }
                }
        }
        return subjectDao.observeByGroupId(groupId)
            .map(subjectMapper::entityToDomain)
    }

}