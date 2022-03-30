package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.denchic45.appVersion.AppVersionService
import com.denchic45.kts.data.*
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.firestore.CourseContentDoc
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SubmissionDoc
import com.denchic45.kts.data.model.mapper.*
import com.denchic45.kts.data.model.room.*
import com.denchic45.kts.data.prefs.*
import com.denchic45.kts.data.prefs.TimestampPreference.Companion.TIMESTAMP_LAST_UPDATE_GROUP_COURSES
import com.denchic45.kts.data.storage.ContentAttachmentStorage
import com.denchic45.kts.data.storage.SubmissionAttachmentStorage
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.CourseContents
import com.denchic45.kts.utils.FieldsComparator
import com.denchic45.kts.utils.toDate
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class CourseRepository @Inject constructor(
    override val context: Context,
    override val appVersionService: AppVersionService,
    override val coroutineScope: CoroutineScope,
    override val userMapper: UserMapper,
    @IoDispatcher override val dispatcher: CoroutineDispatcher,
    override val courseMapper: CourseMapper,
    override val specialtyMapper: SpecialtyMapper,
    override val groupMapper: GroupMapper,
    private val submissionMapper: SubmissionMapper,
    private val coursePreference: CoursePreference,
    private val groupPreference: GroupPreference,
    override val subjectMapper: SubjectMapper,
    override val networkService: NetworkService,
    private val contentAttachmentStorage: ContentAttachmentStorage,
    private val submissionAttachmentStorage: SubmissionAttachmentStorage,
    override val firestore: FirebaseFirestore,
    override val dataBase: DataBase,
    private val userPreference: UserPreference,
    private val timestampPreference: TimestampPreference,
    private val appPreference: AppPreference,
    override val sectionMapper: SectionMapper,
    private val courseContentMapper: CourseContentMapper,
    override val specialtyDao: SpecialtyDao,
    override val userDao: UserDao,
    override val courseDao: CourseDao,
    private val courseContentDao: CourseContentDao,
    override val sectionDao: SectionDao,
    private val submissionDao: SubmissionDao,
    private val submissionCommentDao: SubmissionCommentDao,
    private val contentCommentDao: ContentCommentDao,
    override val groupCourseDao: GroupCourseDao,
    override val subjectDao: SubjectDao,
    override val groupDao: GroupDao,
) : Repository(context), SaveGroupOperation, SaveCourseRepository, RemoveCourseOperation {
    override val groupsRef: CollectionReference = firestore.collection("Groups")
    override val coursesRef: CollectionReference = firestore.collection("Courses")
    private val contentsRef: Query = firestore.collectionGroup("Contents")

    fun find(courseId: String): Flow<Course?> = courseDao.observe(courseId)
        .withSnapshotListener(
            documentReference = coursesRef.document(courseId),
            onDocumentSnapshot = { documentSnapshot: DocumentSnapshot ->
                if (!documentSnapshot.exists()) {
                    courseDao.deleteById(courseId)
                    return@withSnapshotListener
                }
                if (timestampIsNull(documentSnapshot))
                    return@withSnapshotListener
                val courseDoc = documentSnapshot.toObject(CourseDoc::class.java)!!
                coroutineScope.launch {
                    dataBase.withTransaction {
                        if (courseDoc.groupIds.isNotEmpty()) {
                            val querySnapshot = groupsRef.whereIn("id", courseDoc.groupIds)
                                .get()
                                .await()
                            if (timestampsNotNull(querySnapshot))
                                saveGroups(querySnapshot.toObjects(GroupDoc::class.java))
                        }
                        saveCourse(courseDoc)
                    }
                }
            }
        )
        .map {
            dataBase.withTransaction { }
            it?.let { courseMapper.entityToDomain2(it) }
        }
        .distinctUntilChanged()

//    fun find(courseId: String): Flow<Course?> {
//        addListenerRegistration("findCourseById $courseId") {
//            coursesRef.document(courseId).addSnapshotListener { value, error ->
//                coroutineScope.launch(dispatcher) {
//                    value?.let { documentSnapshot ->
//                        if (!value.exists()) {
//                            courseDao.deleteById(courseId)
//                            return@launch
//                        }
//                        if (timestampIsNull(value))
//                            return@launch
//                        val courseDoc = documentSnapshot.toObject(CourseDoc::class.java)!!
//
//                        dataBase.withTransaction {
//                            if (courseDoc.groupIds.isNotEmpty()) {
//                                val querySnapshot = groupsRef.whereIn("id", courseDoc.groupIds)
//                                    .get()
//                                    .await()
//                                if (timestampsNotNull(querySnapshot))
//                                    saveGroups(querySnapshot.toObjects(GroupDoc::class.java))
//                            }
//                            saveCourse(courseDoc)
//                        }
//                    }
//                }
//            }
//        }
//        return courseDao.observe(courseId)
//            .map { it?.let { courseMapper.entityToDomain2(it) } }
//            .distinctUntilChanged()
//    }

    private fun coursesByGroupIdQuery(groupId: String): Query {
        return coursesRef.whereArrayContains("groupIds", groupId)
    }

    private fun coursesByTeacherIdQuery(teacherId: String): Query {
        return coursesRef.whereEqualTo("teacher.id", teacherId)
            .whereGreaterThan(
                "timestamp",
                Date(timestampPreference.lastUpdateTeacherCoursesTimestamp)
            )
    }

    suspend fun observeByYourGroup() {
        combine(
            timestampPreference.observeValue(TIMESTAMP_LAST_UPDATE_GROUP_COURSES, 0L)
                .filter { it != 0L }
                .drop(if (appPreference.coursesLoadedFirstTime) 1 else 0),
            groupPreference.observeValue(GroupPreference.GROUP_ID, "").filter(String::isNotEmpty)
        ) { timestamp, groupId -> timestamp to groupId }
            .collect { (timestamp, groupId) ->
                appPreference.coursesLoadedFirstTime = true
                getCoursesByGroupIdRemotely(groupId)
            }
    }


    fun findByYourAsTeacher(): Flow<List<CourseHeader>> {
        getCoursesByTeacherRemotely(userPreference.id)
        return courseDao.getByTeacherId(userPreference.id)
            .map { courseMapper.entityToDomainHeaders(it) }
    }

    fun findContentByCourseId(courseId: String): Flow<List<DomainModel>> {
        val query = coursesRef.document(courseId).collection("Contents").run {
            val timestampContentsOfCourse = coursePreference.getTimestampContentsOfCourse(courseId)
            if (timestampContentsOfCourse == 0L)
                whereEqualTo("deleted", false)
            else
                whereGreaterThan("timestamp", Date(timestampContentsOfCourse))
        }


        addListenerRegistrationIfNotExist("findContentByCourseId: $courseId") {
            query.addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    if (timestampsNotNull(value)) {
                        coroutineScope.launch(dispatcher) {
                            val courseContents = courseContentMapper.docToEntity(value)

                            dataBase.withTransaction {
                                saveCourseContentsLocal(courseContents)
                            }

                            coursePreference.setTimestampContentsOfCourse(
                                courseId,
                                courseContents.first.maxOf { it.timestamp }.time
                            )
                        }
                    }
                }
            }
        }

        return sectionDao.getByCourseId(courseId)
            .combine(courseContentDao.getByCourseId(courseId)) { sectionEntities, courseContentEntities ->
                CourseContents.sort(
                    courseContentMapper.entityToDomain(courseContentEntities),
                    sectionMapper.entityToDomain(sectionEntities)
                )
            }
            .distinctUntilChanged()
    }

    private suspend fun saveCourseContentsLocal(
        courseContents: Pair<List<CourseContentEntity>, List<CourseContentDoc>>
    ) {

        val removedCourseContents = courseContents.first.filter { it.deleted }
        val remainingCourseContent = courseContents.first - removedCourseContents.toSet()

        removedCourseContents.forEach {
            contentAttachmentStorage.deleteFromLocal(it.id)
            submissionAttachmentStorage.deleteFromLocal(it.id)
        }

        dataBase.withTransaction {
            courseContentDao.upsert(remainingCourseContent)
            courseContentDao.delete(removedCourseContents)

            val submissionEntities = mutableListOf<SubmissionEntity>()
            val contentCommentEntities = mutableListOf<ContentCommentEntity>()
            val submissionCommentEntities = mutableListOf<SubmissionCommentEntity>()

            courseContents.second.forEach {
                submissionDao.deleteByContentId(it.id)
                contentCommentDao.deleteByContentId(it.id)

                it.submissions?.forEach { submissionDoc ->
                    submissionEntities.add(
                        submissionMapper.docToEntity(submissionDoc.value)
                    )
                    submissionCommentEntities.addAll(submissionDoc.value.comments)
                    it.comments?.let { contentComments ->
                        contentCommentEntities.addAll(contentComments)
                    }
                }
            }

            submissionDao.upsert(submissionEntities)
            contentCommentDao.upsert(contentCommentEntities)
            submissionCommentDao.upsert(submissionCommentEntities)
        }
    }

    fun findByGroupId(groupId: String): Flow<List<CourseHeader>> {
        if (groupId != groupPreference.groupId)
            coroutineScope.launch {
                dataBase.withTransaction {
                    getCoursesByGroupIdRemotely(groupId)
                }
            }
        return courseDao.observeCoursesByGroupId(groupId)
            .map { courseMapper.entityToDomainHeaders(it) }
    }

    private suspend fun getCoursesByGroupIdRemotely(groupId: String) {
        coursesByGroupIdQuery(groupId).get().await().let {
            if (!it.isEmpty)
                dataBase.withTransaction {
                    groupCourseDao.deleteByGroup(groupId)
                    saveCourses(it.toObjects(CourseDoc::class.java))
                }
        }
    }

    private fun getCoursesByTeacherRemotely(teacherId: String) {
        coursesByTeacherIdQuery(teacherId).addSnapshotListener { value, error ->
            coroutineScope.launch {
                value?.let { value ->

                    if (!value.isEmpty) {

                        value.toObjects(CourseDoc::class.java).apply {
                            timestampPreference.lastUpdateTeacherCoursesTimestamp =
                                this.maxOf { it.timestamp!!.time }
                            Log.d("lol", "savingCourse: $this")
                            dataBase.withTransaction {
                                saveCourses(this)
                            }
                        }
                    }
                } ?: error?.printStackTrace()
            }
        }
    }

    fun findByYourGroup(): Flow<List<CourseHeader>> {
        return groupPreference.observeGroupId
            .filter(String::isNotEmpty)
            .flatMapLatest { courseDao.observeCoursesByGroupId(it) }
            .map { entity ->
                courseMapper.entityToDomainHeaders(entity)
            }
    }

    suspend fun add(course: Course) {
        requireAllowWriteData()
        val courseDoc = courseMapper.domainToDoc(course)
        val batch = firestore.batch()
        (courseDoc.groupIds).forEach { groupId ->
            batch.update(
                groupsRef.document(groupId),
                "timestamp",
                FieldValue.serverTimestamp()
            )

            batch.update(
                groupsRef.document(groupId),
                "timestampCourses",
                FieldValue.serverTimestamp()
            )
        }
        batch.set(coursesRef.document(courseDoc.id), courseDoc)
        batch.commit().await()
    }

    suspend fun update(course: Course) {
        requireAllowWriteData()
        val batch = firestore.batch()
        val courseDoc = courseMapper.domainToDoc(course)

        val oldCourse = courseMapper.entityToDomain2(courseDao.get(course.id))
        val oldCourseDoc = courseMapper.domainToDoc(oldCourse)

        (oldCourseDoc.groupIds + courseDoc.groupIds).forEach { groupId ->
            batch.update(
                groupsRef.document(groupId),
                "timestamp",
                FieldValue.serverTimestamp()
            )

            batch.update(
                groupsRef.document(groupId),
                "timestampCourses",
                FieldValue.serverTimestamp()
            )
        }

        batch.update(
            coursesRef.document(courseDoc.id),
            FieldsComparator.mapOfDifference(oldCourseDoc, courseDoc)
        )
        batch.commit().await()
    }


    private fun timestampFiledPair() = "timestamp" to FieldValue.serverTimestamp()

    fun findByTypedName(name: String): Flow<List<CourseHeader>> = callbackFlow {
        addListenerRegistration("name") {
            coursesRef
                .whereArrayContains("searchKeys", name.lowercase())
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    val courseDocs = value!!.toObjects(CourseDoc::class.java)
                    launch {
                        dataBase.withTransaction {
                            saveCourses(courseDocs)
                        }
                    }
                    trySend(
                        courseMapper.docToDomain(courseDocs)
                    )
                }
        }
        awaitClose { }
    }


    suspend fun removeGroupFromCourses(groupId: String) {
        requireAllowWriteData()
        val courseDocs = coursesByGroupIdQuery(groupId)
            .get()
            .await()
            .toObjects(CourseDoc::class.java)
        val batch = firestore.batch()
        courseDocs.forEach { courseDoc ->
            batch.update(
                coursesRef.document(courseDoc.id),
                mapOf(
                    timestampFiledPair(),
                    "groupIds" to FieldValue.arrayRemove(groupId)
                )
            )
        }
        batch.commit().await()
    }

    fun findTask(id: String): Flow<Task?> {
        return courseContentDao.get(id)
            .map { taskEntity ->
                taskEntity?.let { existTask ->
                    courseContentMapper.entityToTaskDomain(existTask)
                }
            }
    }

    fun findAttachmentsByContentId(contentId: String): Flow<List<Attachment>> {
        return courseContentDao.getAttachmentsById(contentId)
            .filterNotNull()
            .mapLatest { attachments ->
                contentAttachmentStorage.get(contentId, ListConverter.tolList(attachments))
            }
    }

    suspend fun addTask(task: Task) {
        requireAllowWriteData()
        val attachments = contentAttachmentStorage.addContentAttachments(task.id, task.attachments)
        val order = getLastContentOrderByCourseIdAndSectionId(task.courseId, task.sectionId) + 1024
        coursesRef.document(task.courseId)
            .collection("Contents")
            .document(task.id)
            .set(courseContentMapper.domainToTaskDoc(task).apply {
                this.attachments = attachments
                this.order = order
            }.asMutableMap().apply {
                val studentIdsOfCourseByCourseId =
                    userDao.getStudentIdsOfCourseByCourseId(task.courseId)
                val mapOfEmptySubmissions =
                    studentIdsOfCourseByCourseId
                        .associateWith { studentId ->
                            SubmissionDoc.createNotSubmitted(
                                studentId,
                                task.id,
                                task.courseId
                            )
                        }
                put("submissions", mapOfEmptySubmissions)
                put("notSubmittedByStudentIds", studentIdsOfCourseByCourseId)
            }
            )
            .await()
    }

    private suspend fun getLastContentOrderByCourseIdAndSectionId(
        courseId: String,
        sectionId: String
    ): Long {
        val snapshot = coursesRef.document(courseId).collection("Contents")
            .whereEqualTo("sectionId", sectionId)
            .orderBy("order", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        return if (snapshot.isEmpty) 0L
        else snapshot.documents[0].getLong("order")!!
    }

    suspend fun updateTask(task: Task) {
        requireAllowWriteData()
        val attachments = contentAttachmentStorage.update(task.id, task.attachments)
        val cacheTask = courseContentMapper.entityToTaskDoc(courseContentDao.getSync(task.id))
        val updatedFields =
            FieldsComparator.mapOfDifference(cacheTask, courseContentMapper.domainToDoc(task))
        updatedFields["attachments"] = attachments
        updatedFields["timestamp"] = Date()
        if (updatedFields.containsKey("sectionId"))
            updatedFields["order"] =
                getLastContentOrderByCourseIdAndSectionId(task.courseId, task.sectionId) + 1024
        coursesRef.document(task.courseId)
            .collection("Contents")
            .document(task.id)
            .update(updatedFields)
            .await()
    }

    suspend fun removeCourseContent(taskId: String) {
        requireAllowWriteData()
        contentAttachmentStorage.deleteFilesByContentId(taskId)
        submissionAttachmentStorage.deleteFilesByContentId(taskId)
        getContentDocument(taskId)
            .set(
                mapOf(
                    "id" to taskId,
                    "timestamp" to Date(),
                    "deleted" to true
                )
            )
            .await()
    }

    private suspend fun getContentDocument(contentId: String) =
        coursesRef.document(courseContentDao.getCourseIdByTaskId(contentId))
            .collection("Contents")
            .document(contentId)

    fun findSectionsByCourseId(courseId: String) =
        sectionDao.getByCourseId(courseId).map {
            Log.d("lol", "findSectionsByCourseId MAP: ")
            sectionMapper.entityToDomain(it)
        }

    suspend fun findSection(sectionId: String) =
        sectionMapper.entityToDomain(sectionDao.get(sectionId))

    fun findTaskSubmissionByContentIdAndStudentId(
        taskId: String,
        studentId: String
    ): Flow<Task.Submission> {
        return submissionDao.getByTaskIdAndUserId(taskId, studentId)
            .map {
                it?.let {
                    val attachments = submissionAttachmentStorage.get(
                        it.submissionEntity.contentId,
                        it.submissionEntity.studentId,
                        it.submissionEntity.attachments
                    )
                    submissionMapper.entityToDomain(it, attachments)
                } ?: Task.Submission.createEmpty(
                    contentId = taskId,
                    student = userMapper.entityToDomain(userDao.get(studentId))
                )
            }
            .distinctUntilChanged()
    }

    suspend fun updateSubmissionFromStudent(submission: Task.Submission) {
        requireAllowWriteData()
        val studentId = submission.student.id
        val contentId = submission.contentId

        val attachmentUrls = submissionAttachmentStorage.update(
            contentId,
            studentId,
            attachments = submission.content.attachments
        )

        val submittedDate =
            if (submission.status is Task.SubmissionStatus.Submitted)
                submission.status.submittedDate.toDate()
            else null

        val updatedFields = mapOf(
            "submissions.$studentId.text" to submission.content.text,
            "submissions.$studentId.attachments" to attachmentUrls,
            "submissions.$studentId.status" to submissionMapper.domainToStatus(submission),
            "submissions.$studentId.contentUpdateDate" to submission.contentUpdateDate.toDate(),
            "submissions.$studentId.submittedDate" to submittedDate,
            "submittedByStudentIds" to
                    if (submission.submitted)
                        FieldValue.arrayUnion(studentId)
                    else
                        FieldValue.arrayRemove(studentId),
            "notSubmittedByStudentIds" to
                    if (submission.submitted)
                        FieldValue.arrayRemove(studentId)
                    else
                        FieldValue.arrayUnion(studentId)
        )
        coursesRef.document(courseContentDao.getCourseId(contentId))
            .collection("Contents")
            .document(contentId)
            .update(mapOfSubmissionFields(contentId, studentId) + updatedFields)
            .await()
    }

    suspend fun gradeSubmission(
        taskId: String,
        studentId: String,
        grade: Int,
        teacherId: String = userPreference.id
    ) {
        requireAllowWriteData()
        getContentDocument(taskId).update(
            mapOfSubmissionFields(taskId, studentId)
                    +
                    mapOf(
                        "submissions.$studentId.status" to Task.Submission.Status.GRADED,
                        "submissions.$studentId.gradedDate" to Date(),
                        "submissions.$studentId.grade" to grade,
                        "submissions.$studentId.teacherId" to teacherId,

                        "submittedByStudentIds" to FieldValue.arrayUnion(studentId),
                        "notSubmittedByStudentIds" to FieldValue.arrayRemove(studentId),
                    )
        ).await()
    }

    suspend fun rejectSubmission(
        taskId: String,
        studentId: String,
        cause: String,
        teacherId: String = userPreference.id
    ) {
        requireAllowWriteData()
        getContentDocument(taskId).update(
            mapOfSubmissionFields(taskId, studentId)
                    +
                    mapOf(
                        "timestamp" to FieldValue.serverTimestamp(),
                        "submissions.$studentId.status" to Task.Submission.Status.REJECTED,
                        "submissions.$studentId.cause" to cause,
                        "submissions.$studentId.rejectedDate" to Date(),
                        "submissions.$studentId.teacherId" to teacherId,

                        "submittedByStudentIds" to FieldValue.arrayRemove(studentId),
                        "notSubmittedByStudentIds" to FieldValue.arrayUnion(studentId),
                    )
        ).await()
    }

    private suspend fun mapOfSubmissionFields(
        taskId: String,
        studentId: String,
    ) = mapOf(
        "timestamp" to FieldValue.serverTimestamp(),
        "submissions.$studentId.studentId" to studentId,
        "submissions.$studentId.contentId" to taskId,
        "submissions.$studentId.courseId" to courseContentDao.getCourseId(taskId),
    )

    suspend fun isCourseTeacher(userId: String, courseId: String): Boolean {
        return courseDao.isCourseTeacher(courseId, userId)
    }

    fun findTaskSubmissions(taskId: String): Flow<List<Task.Submission>> {
        return submissionDao.getByTaskId(taskId).map { list ->
            list.map {
                val attachments = submissionAttachmentStorage.get(
                    it.submissionEntity.contentId,
                    it.submissionEntity.studentId,
                    it.submissionEntity.attachments
                )
                submissionMapper.entityToDomain(it, attachments)
            }
        }
            .mapLatest {
                it + submissionDao.getStudentsWithoutSubmission(taskId)
                    .map { userEntity ->
                        Task.Submission.createEmpty(taskId, userMapper.entityToDomain(userEntity))
                    }
            }
            .distinctUntilChanged()
    }

    suspend fun updateCourseSections(sections: List<Section>) {
        requireAllowWriteData()
        coursesRef.document(sections[0].courseId)
            .update("sections", sections)
            .await()
    }

    suspend fun addSection(section: Section) {
        requireAllowWriteData()
        coursesRef.document(section.courseId)
            .update("sections", FieldValue.arrayUnion(section))
            .await()
    }

    suspend fun removeSection(section: Section) {
        requireAllowWriteData()
        val courseRef = coursesRef.document(section.courseId)

        val contentsWithThisSection = courseRef.collection("Contents")
            .whereEqualTo("sectionId", section.id)
            .get()
            .await()

        val batch = firestore.batch()

        contentsWithThisSection.forEach {
            batch.update(
                courseRef.collection("Contents")
                    .document(it.getString("id")!!), "sectionId", ""
            )
        }

        batch.update(courseRef, "sections", FieldValue.arrayRemove(section))
        batch.commit().await()
    }

    suspend fun updateContentOrder(contentId: String, order: Long) {
        requireAllowWriteData()
        getContentDocument(contentId)
            .update(
                mapOf(
                    "order" to order,
                    timestampFiledPair()
                )
            )
            .await()
    }

    fun findUpcomingTasksForYourGroup(): Flow<List<Task>> {
        contentsRef.whereGreaterThanOrEqualTo(
            "completionDate",
            Date()
        )
            .whereArrayContains(
                "notSubmittedByStudentIds",
                userPreference.id
            )
            .limit(10)
            .addSnapshotListener { value, error ->
                coroutineScope.launch {
                    value?.let {
                        dataBase.withTransaction {
                            saveCourseContentsLocal(
                                courseContentMapper.docToEntity(it)
                            )
                        }
                    }
                }

            }
        return courseContentDao.getByGroupIdAndGreaterCompletionDate(
            groupPreference.groupId
        ).map { courseContentMapper.entityToDomainAssignment(it) }
    }

    fun findOverdueTasksForYourGroup(): Flow<List<Task>> {
        return flow {
            val contentsQuerySnapshot =
                contentsRef
                    .whereLessThanOrEqualTo(
                        "completionDate",
                        Date()
                    )
                    .whereArrayContains(
                        "notSubmittedByStudentIds",
                        userPreference.id
                    )
                    .limit(10)
                    .get()
                    .await()
            saveCourseContentsLocal(
                courseContentMapper.docToEntity(contentsQuerySnapshot)
            )

            emitAll(courseContentDao.getByGroupIdAndNotSubmittedUser(
                groupPreference.groupId,
                userPreference.id
            ).map { courseContentMapper.entityToDomainAssignment(it) })
        }
    }

    fun findCompletedTasksForYourGroup(): Flow<List<Task>> {
        return flow {
            contentsRef
                .whereArrayContains(
                    "submittedByStudentIds",
                    userPreference.id
                )
                .limit(10)
                .get()
                .await().apply {
                    saveCourseContentsLocal(
                        courseContentMapper.docToEntity(this)
                    )
                }
            emitAll(courseContentDao.getByGroupIdAndSubmittedUser(
                groupPreference.groupId,
                userPreference.id
            ).map { courseContentMapper.entityToDomainAssignment(it) })

        }
    }
}


class SameCoursesException : Exception()

interface RemoveCourseOperation : PreconditionsRepository {

    val firestore: FirebaseFirestore
    val courseMapper: CourseMapper
    val coursesRef: CollectionReference
    val groupsRef: CollectionReference
    val coroutineScope: CoroutineScope
    val dispatcher: CoroutineDispatcher

    suspend fun removeCourse(courseId: String, groupIds: List<String>) {
        requireAllowWriteData()
        val batch = firestore.batch()
        batch.delete(coursesRef.document(courseId))
        groupIds.forEach { groupId ->
            batch.update(
                groupsRef.document(groupId),
                "timestamp",
                FieldValue.serverTimestamp()
            )

            batch.update(
                groupsRef.document(groupId),
                "timestampCourses",
                FieldValue.serverTimestamp()
            )
        }
        coursesRef.document(courseId).collection("Contents").deleteCollection(10)
        batch.commit().await()
    }
}

interface SaveCourseRepository {

    val courseMapper: CourseMapper
    val userMapper: UserMapper
    val subjectMapper: SubjectMapper
    val sectionMapper: SectionMapper
    val groupDao: GroupDao
    val userDao: UserDao
    val subjectDao: SubjectDao
    val courseDao: CourseDao
    val sectionDao: SectionDao
    val groupCourseDao: GroupCourseDao


    suspend fun saveCourse(courseDoc: CourseDoc) {

        groupCourseDao.deleteByCourse(courseDoc.id)

        userDao.upsert(userMapper.docToEntity(courseDoc.teacher))

        subjectDao.upsert(subjectMapper.docToEntity(courseDoc.subject))

        courseDao.upsert(courseMapper.docToEntity(courseDoc))

        sectionDao.deleteByCourseId(courseDoc.id)

        sectionDao.upsert(
            sectionMapper.docToEntity(courseDoc.sections ?: emptyList())
        )

        groupCourseDao.upsert(
            courseDoc.groupIds
                .filter { groupDao.isExist(it) }
                .map { groupId -> GroupCourseCrossRef(groupId, courseDoc.id) }
        )
    }

    suspend fun saveCourses(courseDocs: List<CourseDoc>) {
        courseDocs.forEach { saveCourse(it) }
    }
}