package com.denchic45.kts.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asFlow
import com.denchic45.kts.data.*
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.firestore.CourseContentDoc
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SubjectTeacherPair
import com.denchic45.kts.data.model.mapper.*
import com.denchic45.kts.data.model.room.*
import com.denchic45.kts.data.prefs.*
import com.denchic45.kts.data.prefs.TimestampPreference.Companion.TIMESTAMP_LAST_UPDATE_GROUP_COURSES
import com.denchic45.kts.data.storage.ContentAttachmentStorage
import com.denchic45.kts.data.storage.SubmissionAttachmentStorage
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.CourseContents
import com.denchic45.kts.utils.FieldsComparator
import com.google.firebase.firestore.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class CourseRepository @Inject constructor(
    context: Context,
    private val userMapper: UserMapper,
    private val externalScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val courseMapper: CourseMapper,
    private val submissionMapper: SubmissionMapper,
    private val coursePreference: CoursePreference,
    private val groupPreference: GroupPreference,
    private val subjectMapper: SubjectMapper,
    override val networkService: NetworkService,
    private val contentAttachmentStorage: ContentAttachmentStorage,
    private val submissionAttachmentStorage: SubmissionAttachmentStorage,
    private val firestore: FirebaseFirestore,
    private val database: DataBase,
    private val userPreference: UserPreference,
    private val timestampPreference: TimestampPreference,
    private val appPreference: AppPreference,
    private val groupMapper: GroupMapper,
    private val specialtyMapper: SpecialtyMapper,
    private val sectionMapper: SectionMapper,
    private val courseContentMapper: CourseContentMapper,
    private val courseDao: CourseDao,
    private val courseContentDao: CourseContentDao,
    private val sectionDao: SectionDao,
    private val submissionDao: SubmissionDao,
    private val submissionCommentDao: SubmissionCommentDao,
    private val contentCommentDao: ContentCommentDao,
    private val groupCourseDao: GroupCourseDao,
    private val subjectDao: SubjectDao,
    private val groupDao: GroupDao,
    private val userDao: UserDao,
    private val specialtyDao: SpecialtyDao,
) : Repository(context) {
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val coursesRef: CollectionReference = firestore.collection("Courses")


    fun find(courseId: String): Flow<Course> {
        addListenerRegistration("findCourseById $courseId") {
            coursesRef.document(courseId).addSnapshotListener { value, error ->
                externalScope.launch(dispatcher) {
                    value?.let {
                        if (!value.exists()) {
                            courseDao.deleteById(courseId)
                            return@launch
                        }
                        val courseDoc = it.toObject(CourseDoc::class.java)!!
                        database.runInTransaction {
                            launch {
                                groupCourseDao.deleteByCourse(courseDoc.id)

                                subjectDao.upsert(
                                    subjectMapper.docToEntity(courseDoc.subject)
                                )
                                userDao.upsert(userMapper.docToEntity(courseDoc.teacher))

                                courseDao.upsert(courseMapper.docToEntity(courseDoc))

                                sectionDao.upsert(sectionMapper.docToEntity(courseDoc.sections))

                                courseDoc.groupIds.let { groupIds ->
                                    if (groupIds.isNotEmpty()) {
                                        val groupDocs = groupsRef.whereIn("id", groupIds)
                                            .get()
                                            .await().toObjects(GroupDoc::class.java)
                                        groupDao.upsert(groupMapper.docToEntity(groupDocs))
                                        specialtyDao.upsert(
                                            specialtyMapper.docToEntity(
                                                groupDocs.map(GroupDoc::specialty)
                                            )
                                        )
                                        groupCourseDao.upsert(
                                            groupIds.map { groupId ->
                                                GroupCourseCrossRef(groupId, courseDoc.id)
                                            }
                                        )
                                    }
                                }
                            }
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


    suspend fun observeByYouGroup() {
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
    fun findByYourAsTeacher(): Flow<List<Course>> {
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
            .map { courseMapper.entityToDomain(it) }
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

        return sectionDao.getByCourseId(courseId)
            .combine(courseContentDao.getByCourseId(courseId)) { sectionEntities, courseContentEntities ->
                CourseContents.sort(
                    courseContentMapper.entityToDomain(courseContentEntities),
                    sectionMapper.entityToDomain(sectionEntities)
                )

                //todo отсортировать секции с курсами
            }
    }

    private fun saveCourseContentsLocal(
        courseContents: Pair<MutableList<CourseContentEntity>, MutableList<CourseContentDoc>>
    ) {

        val removedCourseContents = courseContents.first.filter { it.deleted }
        val remainingCourseContent = courseContents.first - removedCourseContents.toSet()

        removedCourseContents.forEach {
            contentAttachmentStorage.deleteFromLocal(it.id)
        }

        database.runInTransaction {
            externalScope.launch(dispatcher) {
                courseContentDao.upsert(remainingCourseContent)
                courseContentDao.delete(removedCourseContents)

                val submissionEntities = mutableListOf<SubmissionEntity>()
                val contentCommentEntities = mutableListOf<ContentCommentEntity>()
                val submissionCommentEntities =
                    mutableListOf<SubmissionCommentEntity>()

                courseContents.second.forEach {

                    submissionDao.deleteByContentId(it.id)
                    contentCommentDao.deleteByContentId(it.id)

                    it.submissions?.forEach { submissionDoc ->
                        submissionEntities.add(
                            submissionMapper.docToEntity(submissionDoc)
                        )
                        submissionCommentEntities.addAll(submissionDoc.comments)
                        it.comments?.let { contentComments ->
                            contentCommentEntities.addAll(
                                contentComments
                            )
                        }
                    }
                }
                submissionDao.upsert(submissionEntities)
                contentCommentDao.upsert(contentCommentEntities)
                submissionCommentDao.upsert(submissionCommentEntities)
            }
        }
    }

    private fun saveCourseOfTeacher(courseDocs: List<CourseDoc>, teacherId: String) {
//        database.runInTransaction {
        externalScope.launch(dispatcher) {
            saveCourses(courseDocs)
            deleteMissingCoursesOfTeacher(courseDocs, teacherId)
        }
//        }
    }

    private fun deleteMissingCoursesOfTeacher(courseDocs: List<CourseDoc>, teacherId: String) {
        courseDao.deleteMissingByTeacher(courseDocs.map(CourseDoc::id), teacherId)
        subjectDao.deleteUnrelatedByCourse()
        groupDao.deleteUnrelatedByCourse()
    }

    private fun saveCoursesOfGroup(courseDocs: List<CourseDoc>, groupId: String) {
//        database.runInTransaction {
        externalScope.launch(dispatcher) {
            saveCourses(courseDocs)
            deleteMissingCoursesOfGroup(courseDocs, groupId)
        }
//        }
    }

    private fun deleteMissingCoursesOfGroup(courseDocs: List<CourseDoc>, groupId: String) {
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
                .map { groupId ->
                    GroupCourseCrossRef(groupId, courseDoc.id)
                }
        }

        userDao.upsert(teacherEntities)
        subjectDao.upsert(subjectEntities)
        courseDao.upsert(courseEntities)
        sectionDao.upsert(courseDocs.flatMap { sectionMapper.docToEntity(it.sections) })
        groupCourseDao.upsert(groupWithCourseEntities)
    }

    fun findByGroupId(groupId: String): Flow<List<Course>> {
        if (groupId != groupPreference.groupId)
            getCoursesByGroupIdRemotely(groupId)
        return courseDao.getCoursesByGroupId(groupId).asFlow()
            .map { courseMapper.entityToDomain2(it) }
    }

    private fun getCoursesByGroupIdRemotely(groupId: String) {
        externalScope.launch(dispatcher) {
            coursesByGroupIdQuery(groupId).get().await().let {
                saveCoursesOfGroup(
                    it.toObjects(CourseDoc::class.java),
                    groupId
                )
            }
        }
    }

    fun findByTeacherId(teacherId: String): Flow<List<Course>> {
        if (teacherId != userPreference.id)
            getCoursesByTeacherRemotely(teacherId)
        return courseDao.getByTeacherId(teacherId)
            .map { courseMapper.entityToDomain(it) }
    }

    private fun getCoursesByTeacherRemotely(teacherId: String) {
        externalScope.launch(dispatcher) {
            coursesByTeacherIdQuery(teacherId).get().await().let {
                saveCourseOfTeacher(
                    it.toObjects(CourseDoc::class.java),
                    teacherId
                )
            }
        }
    }


    fun findByYouGroup(): LiveData<List<Course>> {
        return Transformations.map(
            courseDao.getCoursesByGroupId(groupPreference.groupId)
        ) { entity: List<CourseWithSubjectWithTeacherAndGroupsEntities> ->
            courseMapper.entityToDomain2(entity)
        }
    }

    suspend fun add(course: Course) {
        checkInternetConnection()
        val courseDoc = courseMapper.domainToDoc(course)
        val refsWithUpdatedFields = addCoursesToGroups(courseDoc)

        val batch = firestore.batch()
        batch.set(coursesRef.document(courseDoc.id), courseDoc)
        refsWithUpdatedFields.forEach { batch.update(it.key, it.value) }
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

            val refsWithUpdatedFields = updateCourseToGroups(oldCourseDoc, courseDoc) +
                    removeCourseToRemovedGroups(oldCourseDoc, courseDoc)

            refsWithUpdatedFields.forEach {
                batch.update(it.key, it.value)
            }
            externalScope.launch(dispatcher) { batch.commit().await() }
        }
    }

    private suspend fun addCoursesToGroups(
        courseDoc: CourseDoc
    ): Map<DocumentReference, Map<String, Any>> {
        return courseDoc.groupIds
            .map { groupId -> addCourseToGroup(groupId, courseDoc) }
            .map { it.first to it.second }
            .toMap()
    }

    private suspend fun addCourseToGroup(
        groupId: String,
        courseDoc: CourseDoc
    ): Pair<DocumentReference, Map<String, Any>> {
        val groupDoc = groupsRef.document(groupId)
            .get()
            .await().toObject(GroupDoc::class.java)!!
        return groupsRef.document(groupId) to mapOfAddedFieldsCourse(
            groupDoc,
            courseDoc
        )
    }

    private fun mapOfAddedFieldsCourse(
        groupDoc: GroupDoc,
        courseDoc: CourseDoc
    ): Map<String, Any> {
        val courseField = SubjectTeacherPair(
            courseDoc.subject.id,
            courseDoc.teacher.id
        )
        val groupHasNotContainCourse = !groupDoc.courses!!.contains(courseField)
        if (groupHasNotContainCourse) {
            return mapOf(
                timestampFiledPair(),
                "timestampCourses" to FieldValue.serverTimestamp(),
                "courses" to FieldValue.arrayUnion(courseField),
                "subjects.${courseDoc.subject.id}" to courseDoc.subject,
                "teachers.${courseDoc.teacher.id}" to courseDoc.teacher,

                "teacherIds" to FieldValue.arrayUnion(courseDoc.teacher.id)
            )
        } else {
            throw SameCoursesException()
        }
    }

    private suspend fun updateCourseToGroups(
        oldCourseDoc: CourseDoc, courseDoc: CourseDoc
    ): Map<DocumentReference, Map<String, Any>> {
        val updatedSubjectOrTeacherInCourse = !oldCourseDoc.equalSubjectsAndTeachers(courseDoc)
        val map = courseDoc.groupIds.map { groupId ->
            if (updatedSubjectOrTeacherInCourse) {
                updateCourseToGroup(groupId, oldCourseDoc, courseDoc)
            } else {
                groupsRef.document(groupId) to mutableMapOf<String, Any>(
                    timestampFiledPair(),
                    "timestampCourses" to FieldValue.serverTimestamp()
                )
            }
        }
        return map.map { it.first to it.second }
            .toMap()
    }

    private suspend fun updateCourseToGroup(
        groupId: String,
        oldCourseDoc: CourseDoc,
        courseDoc: CourseDoc
    ): Pair<DocumentReference, Map<String, Any>> {
        val groupDoc = groupsRef.document(groupId)
            .get()
            .await().toObject(GroupDoc::class.java)!!

        val oldCourseField = SubjectTeacherPair(
            oldCourseDoc.subject.id,
            oldCourseDoc.teacher.id
        )
        return groupsRef.document(groupId) to
                mapOfDeletedCourseFields(
                    groupDoc,
                    oldCourseDoc
                ) + mapOfAddedFieldsCourse(
            groupDoc.apply {
                courses = courses!!.toMutableList().apply {
                    removeIf { courseFieldInDoc ->
                        courseFieldInDoc == oldCourseField
                    }
                }.toList()
            },
            courseDoc
        )
    }

    private suspend fun removeCourseToRemovedGroups(
        oldCourseDoc: CourseDoc, courseDoc: CourseDoc
    ): Map<DocumentReference, Map<String, Any>> {
        val removedGroupIds = oldCourseDoc.groupIds.minus(courseDoc.groupIds)
        return if (removedGroupIds.isNotEmpty())
            removedGroupIds.map { groupId ->
                removeCourseToGroup(groupId, oldCourseDoc)
            }.map { it.first to it.second }
                .toMap()
        else emptyMap()
    }

    suspend fun remove(course: Course) {
        checkInternetConnection()
        val batch = firestore.batch()
        val courseDoc = courseMapper.domainToDoc(course)
        batch.delete(coursesRef.document(courseDoc.id))
        val refsWithUpdatedFields = removeCoursesToGroups(courseDoc)

        refsWithUpdatedFields.forEach {
            batch.update(it.key, it.value)
        }
        externalScope.launch(dispatcher) { batch.commit().await() }
    }

    private suspend fun removeCoursesToGroups(
        oldCourseDoc: CourseDoc
    ): Map<DocumentReference, Map<String, Any>> {
        return oldCourseDoc.groupIds
            .map { groupId -> removeCourseToGroup(groupId, oldCourseDoc) }
            .map { it.first to it.second }
            .toMap()

    }

    private suspend fun removeCourseToGroup(
        groupId: String,
        oldCourseDoc: CourseDoc
    ): Pair<DocumentReference, Map<String, Any>> {
        val groupDoc = groupsRef.document(groupId)
            .get()
            .await()
            .toObject(GroupDoc::class.java)!!
        return groupsRef.document(groupId) to mapOfDeletedCourseFields(
            groupDoc,
            oldCourseDoc
        )

    }

    private fun mapOfDeletedCourseFields(
        groupDoc: GroupDoc,
        oldCourseDoc: CourseDoc
    ): MutableMap<String, Any> {

        val oldCourseField = SubjectTeacherPair(
            oldCourseDoc.subject.id,
            oldCourseDoc.teacher.id
        )

        val updatedFieldsGroup = mutableMapOf<String, Any>(
            timestampFiledPair(),
            "timestampCourses" to FieldValue.serverTimestamp()
        )

        updatedFieldsGroup["courses"] = FieldValue.arrayRemove(oldCourseField)
        // remove if not use
        val groupCoursesWithoutOldCourse = groupDoc.courses!!.toMutableList()
        groupCoursesWithoutOldCourse.removeIf { courseFieldInDoc -> courseFieldInDoc == oldCourseField }
        val subjectInOldCourseNoLongerUsed =
            groupCoursesWithoutOldCourse.none { courseFieldInDoc ->
                courseFieldInDoc.subjectId == oldCourseField.subjectId
            }
        val teacherInOldCourseNoLongerUsed =
            groupCoursesWithoutOldCourse.none { courseFieldInDoc ->
                courseFieldInDoc.teacherId == oldCourseField.teacherId
            }
        if (subjectInOldCourseNoLongerUsed) {
            updatedFieldsGroup["subjects.${oldCourseDoc.subject.id}"] = FieldValue.delete()
        }
        if (teacherInOldCourseNoLongerUsed) {
            updatedFieldsGroup["teachers.${oldCourseDoc.teacher.id}"] = FieldValue.delete()
            updatedFieldsGroup["teacherIds"] = FieldValue.arrayRemove(oldCourseDoc.teacher.id)
        }
        return updatedFieldsGroup
    }

    private fun updateGroupsByCourse(
        groupIds: List<String>,
        batch: WriteBatch
    ) {
        for (groupId in groupIds) {
            batch.update(
                groupsRef.document(groupId),
                mapOf(
                    timestampFiledPair(),
                    "timestampCourses" to FieldValue.serverTimestamp()
                )
            )
        }
    }

    private fun timestampFiledPair() = "timestamp" to FieldValue.serverTimestamp()


    fun findByTypedName(name: String): Flow<Resource2<List<Course>>> = callbackFlow {
        addListenerRegistration("name") {
            coursesRef
                .whereArrayContains("searchKeys", name.lowercase())
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    val courseDocs = value!!.toObjects(CourseDoc::class.java)
                    externalScope.launch(dispatcher) { saveCourses(courseDocs) }

                    trySend(
                        Resource2.Success(courseMapper.docToDomain(courseDocs))
                    )
                }
        }
        awaitClose { }
    }

    fun findWhereYouTeacher(): Flow<List<Course>> {
        return courseDao.getByTeacherId(userPreference.id)
            .map { courseMapper.entityToDomain(it) }
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

    fun findTask(id: String): Flow<Task> {
        return courseContentDao.get(id)
            .map(courseContentMapper::entityToTaskDomain)
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
            })
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
        val cacheTask = courseContentMapper.entityToTaskDomain(courseContentDao.getSync(task.id))
        val updatedFields = FieldsComparator.mapOfDifference(cacheTask, task)
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

    suspend fun removeCourseContent(courseContent: CourseContent) {
        contentAttachmentStorage.deleteFilesByContentId(courseContent.id)
        coursesRef.document(courseContent.courseId)
            .collection("Contents")
            .document(courseContent.id)
            .set(
                mapOf(
                    "id" to courseContent.id,
                    "timestamp" to Date(),
                    "deleted" to true
                )
            )
            .await()
    }

    fun findSectionsByCourseId(courseId: String) =
        sectionDao.getByCourseId(courseId).map { sectionMapper.entityToDomain(it) }

    suspend fun findSection(sectionId: String) =
        sectionMapper.entityToDomain(sectionDao.get(sectionId))

    fun findTaskSubmission(taskId: String, userId: String): Flow<Task.Submission> {
        return submissionDao.getByTaskIdAndUserId(taskId, userId)
            .map {
                it?.let {
                    val attachments = submissionAttachmentStorage.get(
                        it.submissionEntity.contentId,
                        it.submissionEntity.studentId,
                        it.submissionEntity.attachments
                    )
                    submissionMapper.entityToDomain(it, attachments)
                } ?: Task.Submission.Nothing(userMapper.entityToDomain(userDao.getSync(userId)))
            }
    }
}

class SameCoursesException : Exception()