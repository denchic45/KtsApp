package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asFlow
import androidx.room.withTransaction
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
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
    context: Context,
    override val userMapper: UserMapper,
    private val externalScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val courseMapper: CourseMapper,
    override val specialtyMapper: SpecialtyMapper,
    override val groupMapper: GroupMapper,
    private val submissionMapper: SubmissionMapper,
    private val coursePreference: CoursePreference,
    private val groupPreference: GroupPreference,
    private val subjectMapper: SubjectMapper,
    override val networkService: NetworkService,
    private val contentAttachmentStorage: ContentAttachmentStorage,
    private val submissionAttachmentStorage: SubmissionAttachmentStorage,
    private val firestore: FirebaseFirestore,
    override val dataBase: DataBase,
    private val userPreference: UserPreference,
    private val timestampPreference: TimestampPreference,
    private val appPreference: AppPreference,
    private val sectionMapper: SectionMapper,
    private val courseContentMapper: CourseContentMapper,
    override val specialtyDao: SpecialtyDao,
    override val userDao: UserDao,
    private val courseDao: CourseDao,
    private val courseContentDao: CourseContentDao,
    private val sectionDao: SectionDao,
    private val submissionDao: SubmissionDao,
    private val submissionCommentDao: SubmissionCommentDao,
    private val contentCommentDao: ContentCommentDao,
    private val groupCourseDao: GroupCourseDao,
    private val subjectDao: SubjectDao,
    override val groupDao: GroupDao,
) : Repository(context), IGroupRepository {
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val coursesRef: CollectionReference = firestore.collection("Courses")
    private val contentsRef: Query = firestore.collectionGroup("Contents")

    fun find(courseId: String): Flow<Course> {
        addListenerRegistration("findCourseById $courseId") {
            coursesRef.document(courseId).addSnapshotListener { value, error ->
                externalScope.launch(dispatcher) {
                    value?.let {
                        if (!value.exists()) {
                            courseDao.deleteById(courseId)
                            return@launch
                        }
                        if (timestampIsNull(value))
                            return@launch
                        val courseDoc = it.toObject(CourseDoc::class.java)!!

                        dataBase.withTransaction {
                            saveCourses(listOf(courseDoc))

                            val querySnapshot = groupsRef.whereIn("id", courseDoc.groupIds)
                                .get()
                                .await()
                            if (timestampsNotNull(querySnapshot))
                                saveUsersAndGroupsAndSubjectsOfTeacher(
                                    querySnapshot.toObjects(GroupDoc::class.java),
                                    userPreference.id
                                )
                        }
                    }
                }
            }
        }
        return courseDao.get(courseId)
            .map { courseMapper.entityToDomain2(it) }
            .distinctUntilChanged()
    }

    private fun coursesByGroupIdQuery(groupId: String): Query {
        return coursesRef.whereArrayContains("groupIds", groupId)
    }

    private fun coursesByTeacherIdQuery(teacherId: String): Query {
        return coursesRef.whereEqualTo("teacher.id", teacherId)
    }


    suspend fun observeByYourGroup() {
        timestampPreference.observeValue(TIMESTAMP_LAST_UPDATE_GROUP_COURSES, 0L)
            .filter { it != 0L }
            .drop(if (appPreference.coursesLoadedFirstTime) 1 else 0)
            .collect {
                appPreference.coursesLoadedFirstTime = true
                getCoursesByGroupIdRemotely(groupPreference.groupId)
            }
    }

    // todo проверить получше
// Сначала мы ищем хотя бы один курс с обновленным timestamp, и если находим, то начинаем прослушивать
// все курсы данного преподавателя
    fun findByYourAsTeacher(): Flow<List<CourseHeader>> {
        addListenerRegistration("findOneCourseByTimestamp") {
            coursesRef.whereGreaterThan(
                "timestamp",
                Date(timestampPreference.lastUpdateTeacherCoursesTimestamp)
            )
                .limit(1)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener { value, error ->
                    error?.let {
                        it.printStackTrace()
                        throw it
                    }
                    if (!value!!.isEmpty) {
                        timestampPreference.lastUpdateTeacherCoursesTimestamp =
                            value.documents[0].toObject(CourseDoc::class.java)!!
                                .timestamp!!
                                .time
                        getCoursesByTeacherRemotely(userPreference.id)
                    }
                }
        }
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
                        externalScope.launch(dispatcher) {
                            val courseContents = courseContentMapper.docToEntity(value)

                            saveCourseContentsLocal(courseContents)

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

    private suspend fun saveCourseOfTeacher(courseDocs: List<CourseDoc>, teacherId: String) {
        saveCourses(courseDocs)
        deleteMissingCoursesOfTeacher(courseDocs, teacherId)
    }

    private suspend fun deleteMissingCoursesOfTeacher(courseDocs: List<CourseDoc>, teacherId: String) {
        courseDao.deleteMissingByTeacher(courseDocs.map(CourseDoc::id), teacherId)
        subjectDao.deleteUnrelatedByCourse()
        groupDao.deleteUnrelatedByCourse()
    }

    private suspend fun saveCoursesOfGroup(courseDocs: List<CourseDoc>, groupId: String) {
        saveCourses(courseDocs)
        deleteMissingCoursesOfGroup(courseDocs, groupId)
    }

    private suspend fun deleteMissingCoursesOfGroup(courseDocs: List<CourseDoc>, groupId: String) {
        groupCourseDao.deleteMissingByGroup(courseDocs.map(CourseDoc::id), groupId)
        courseDao.deleteUnrelatedByGroup()
        subjectDao.deleteUnrelatedByCourse()
        userDao.deleteUnrelatedTeachersByCourseOrGroupAsCurator()
    }

    private suspend fun saveCourses(courseDocs: List<CourseDoc>) {
        val courseEntities = courseMapper.docToEntity(courseDocs)
        val teacherEntities = courseDocs.map { userMapper.docToEntity(it.teacher) }
        val subjectEntities = courseDocs.map { subjectMapper.docToEntity(it.subject) }
        val groupWithCourseEntities = courseDocs.flatMap { courseDoc ->
            courseDoc.groupIds
                .filter { groupDao.isExistSync(it) }
                .map { groupId -> GroupCourseCrossRef(groupId, courseDoc.id) }
        }
        userDao.upsert(teacherEntities)
        subjectDao.upsert(subjectEntities)
        courseDao.upsert(courseEntities)

        sectionDao.upsert(courseDocs.flatMap {
            sectionMapper.docToEntity(it.sections ?: emptyList())
        })
        sectionDao.deleteMissing(courseDocs.flatMap { it.sections ?: emptyList() }.map { it.id })
        groupCourseDao.upsert(groupWithCourseEntities)
    }

    fun findByGroupId(groupId: String): Flow<List<CourseHeader>> {
        if (groupId != groupPreference.groupId)
            getCoursesByGroupIdRemotely(groupId)
        return courseDao.getCoursesByGroupId(groupId).asFlow()
            .map { courseMapper.entityToDomainHeaders(it) }
    }

    private fun getCoursesByGroupIdRemotely(groupId: String) {
        externalScope.launch(dispatcher) {
            coursesByGroupIdQuery(groupId).get().await().let {
                saveCoursesOfGroup(it.toObjects(CourseDoc::class.java), groupId)
            }
        }
    }

    private fun getCoursesByTeacherRemotely(teacherId: String) {
        externalScope.launch(dispatcher) {
            coursesByTeacherIdQuery(teacherId).get().await().let {
                saveCourseOfTeacher(it.toObjects(CourseDoc::class.java), teacherId)
            }
        }
    }


    fun findByYourGroup(): LiveData<List<CourseHeader>> {
        return Transformations.map(
            courseDao.getCoursesByGroupId(groupPreference.groupId)
        ) { entity ->
            courseMapper.entityToDomainHeaders(entity)
        }
    }

    suspend fun add(course: Course) {
        checkInternetConnection()
        val courseDoc = courseMapper.domainToDoc(course)
        val batch = firestore.batch()
        batch.set(coursesRef.document(courseDoc.id), courseDoc)
        externalScope.launch(dispatcher) { batch.commit().await() }
    }

    suspend fun update(course: Course) {
        checkInternetConnection()
        externalScope.launch(dispatcher) {
            val batch = firestore.batch()
            val courseDoc = courseMapper.domainToDoc(course)

            val oldCourse = courseMapper.entityToDomain2(courseDao.getSync(course.id))
            val oldCourseDoc = courseMapper.domainToDoc(oldCourse)

            batch.update(
                coursesRef.document(courseDoc.id),
                FieldsComparator.mapOfDifference(oldCourseDoc, courseDoc)
            )

            externalScope.launch(dispatcher) { batch.commit().await() }
        }
    }

    suspend fun remove(course: Course) {
        checkInternetConnection()
        val batch = firestore.batch()
        val courseDoc = courseMapper.domainToDoc(course)
        batch.delete(coursesRef.document(courseDoc.id))
        externalScope.launch(dispatcher) { batch.commit().await() }
    }

    private fun timestampFiledPair() = "timestamp" to FieldValue.serverTimestamp()

    fun findByTypedName(name: String): Flow<Resource<List<CourseHeader>>> = callbackFlow {
        addListenerRegistration("name") {
            coursesRef
                .whereArrayContains("searchKeys", name.lowercase())
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    val courseDocs = value!!.toObjects(CourseDoc::class.java)
                    externalScope.launch(dispatcher) { saveCourses(courseDocs) }
                    trySend(
                        Resource.Success(courseMapper.docToDomain(courseDocs))
                    )
                }
        }
        awaitClose { }
    }

    suspend fun removeGroup(group: Group) {
        val courseDocs = coursesRef.whereArrayContains("groupIds", group.id)
            .get()
            .await()
            .toObjects(CourseDoc::class.java)
        val batch = firestore.batch()
        courseDocs.forEach { courseDoc ->
            batch.update(
                coursesRef.document(courseDoc.id),
                mapOf(
                    timestampFiledPair(),
                    "groupIds" to FieldValue.arrayRemove(group.id)
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
        val attachments = contentAttachmentStorage.addContentAttachments(task.id, task.attachments)
        val order = getLastContentOrderByCourseIdAndSectionId(task.courseId, task.sectionId) + 1024
        coursesRef.document(task.courseId)
            .collection("Contents")
            .document(task.id)
            .set(courseContentMapper.domainToTaskDoc(task).apply {
                this.attachments = attachments
                this.order = order
            }.asMutableMap().apply {
                val mapOfEmptySubmissions =
                    userDao.getStudentIdsOfCourseByCourseId(task.courseId)
                        .associateWith { studentId ->
                            SubmissionDoc.createNotSubmitted(
                                studentId,
                                task.id,
                                task.courseId
                            )
                        }
                put("submissions", mapOfEmptySubmissions)
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
                    student = userMapper.entityToDomain(userDao.getSync(studentId))
                )
            }
            .distinctUntilChanged()
    }

    suspend fun updateSubmissionFromStudent(submission: Task.Submission) {
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
            "submissions.$studentId.submittedDate" to submittedDate
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
        getContentDocument(taskId).update(
            mapOfSubmissionFields(taskId, studentId)
                    +
                    mapOf(
                        "submissions.$studentId.status" to Task.Submission.Status.GRADED,
                        "submissions.$studentId.gradedDate" to Date(),
                        "submissions.$studentId.grade" to grade,
                        "submissions.$studentId.teacherId" to teacherId,
                    )
        ).await()
    }

    suspend fun rejectSubmission(
        taskId: String,
        studentId: String,
        cause: String,
        teacherId: String = userPreference.id
    ) {
        getContentDocument(taskId).update(
            mapOfSubmissionFields(taskId, studentId)
                    +
                    mapOf(
                        "timestamp" to FieldValue.serverTimestamp(),
                        "submissions.$studentId.status" to Task.Submission.Status.REJECTED,
                        "submissions.$studentId.cause" to cause,
                        "submissions.$studentId.rejectedDate" to Date(),
                        "submissions.$studentId.teacherId" to teacherId,
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
        coursesRef.document(sections[0].courseId)
            .update("sections", sections)
            .await()
    }

    suspend fun addSection(section: Section) {
        coursesRef.document(section.courseId)
            .update("sections", FieldValue.arrayUnion(section))
            .await()
    }

    suspend fun removeSection(section: Section) {
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
        return flow {
            courseDao.getCourseIdsByGroupId(groupPreference.groupId)
                .map { it.chunked(10) }
                .collect { courseIdsLists ->
                    courseIdsLists.forEach { courseIds ->
                        val contentsQuerySnapshot =
                            contentsRef.whereIn("courseId", courseIds)
                                .whereGreaterThanOrEqualTo(
                                    "completionDate",
                                    Date()
                                )
                                .limit(10)
                                .get()
                                .await()
                        saveCourseContentsLocal(
                            courseContentMapper.docToEntity(contentsQuerySnapshot)
                        )
                    }

                    emitAll(courseContentDao.getByGroupIdAndGreaterCompletionDate(
                        groupPreference.groupId
                    ).map { courseContentMapper.entityToDomainAssignment(it) })
                }
        }
    }

    fun findOverdueTasksForYourGroup(): Flow<List<Task>> {
        return flow {
            courseDao.getCourseIdsByGroupId(groupPreference.groupId)
                .map { it.chunked(10) }
                .collect { courseIdsLists ->
                    courseIdsLists.forEach { courseIds ->
                        val contentsQuerySnapshot =
                            contentsRef.whereIn("courseId", courseIds)
                                .whereLessThanOrEqualTo(
                                    "completionDate",
                                    Date()
                                )
                                .whereEqualTo(
                                    "submissions.${userPreference.id}.status",
                                    Task.Submission.Status.NOT_SUBMITTED
                                )
                                .limit(10)
                                .get()
                                .await()
                        saveCourseContentsLocal(
                            courseContentMapper.docToEntity(contentsQuerySnapshot)
                        )
                    }

                    emitAll(courseContentDao.getByGroupIdAndNotSubmittedUser(
                        groupPreference.groupId,
                        userPreference.id
                    ).map { courseContentMapper.entityToDomainAssignment(it) })
                }
        }
    }
}

class SameCoursesException : Exception()