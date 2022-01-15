package com.denchic45.kts.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asFlow
import com.denchic45.kts.data.*
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SubjectTeacherPair
import com.denchic45.kts.data.model.mapper.*
import com.denchic45.kts.data.model.room.CourseWithSubjectWithTeacherAndGroups
import com.denchic45.kts.data.model.room.GroupCourseCrossRef
import com.denchic45.kts.data.model.room.ListConverter
import com.denchic45.kts.data.prefs.*
import com.denchic45.kts.data.prefs.TimestampPreference.Companion.TIMESTAMP_LAST_UPDATE_GROUP_COURSES
import com.denchic45.kts.data.storage.AttachmentStorage
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.CourseContents
import com.denchic45.kts.utils.MembersComparator
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
    private val coursePreference: CoursePreference,
    private val groupPreference: GroupPreference,
    private val subjectMapper: SubjectMapper,
    override val networkService: NetworkService,
    private val attachmentStorage: AttachmentStorage,
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
    private val groupCourseDao: GroupCourseDao,
    private val subjectDao: SubjectDao,
    private val groupDao: GroupDao,
    private val userDao: UserDao,
    private val specialtyDao: SpecialtyDao,
) : Repository(context) {
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val coursesRef: CollectionReference = firestore.collection("Courses")


    fun find(courseUuid: String): Flow<Course> {
        addListenerRegistration("findCourseByUuid $courseUuid") {
            coursesRef.document(courseUuid).addSnapshotListener { value, error ->
                externalScope.launch(dispatcher) {
                    value?.let {
                        if (!value.exists()) {
                            courseDao.deleteById(courseUuid)
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

                                courseDoc.groupIds.let { groupUuids ->
                                    if (groupUuids.isNotEmpty()) {
                                        val groupDocs = groupsRef.whereIn("id", groupUuids)
                                            .get()
                                            .await().toObjects(GroupDoc::class.java)
                                        groupDao.upsert(groupMapper.docToEntity(groupDocs))
                                        specialtyDao.upsert(
                                            specialtyMapper.docToEntity(
                                                groupDocs.map(GroupDoc::specialty)
                                            )
                                        )
                                        groupCourseDao.upsert(
                                            groupUuids.map { groupUuid ->
                                                GroupCourseCrossRef(groupUuid, courseDoc.id)
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
        return courseDao.get(courseUuid)
            .map { courseMapper.entityToDomain(it) }
            .distinctUntilChanged()
    }

    private fun coursesByGroupUuidQuery(groupUuid: String): Query {
        return coursesRef.whereArrayContains("groupIds", groupUuid)
    }

    private fun coursesByTeacherUuidQuery(teacherUuid: String): Query {
        return coursesRef.whereEqualTo("teacher.uuid", teacherUuid)
    }


    suspend fun observeByYouGroup() {
        timestampPreference.observeValue(TIMESTAMP_LAST_UPDATE_GROUP_COURSES, 0L)
            .filter { it != 0L }
            .drop(if (appPreference.coursesLoadedFirstTime) 1 else 0)
            .collect {
                appPreference.coursesLoadedFirstTime = true
                getCoursesByGroupUuidRemotely(groupPreference.groupId)
            }
    }

    // todo проверить получше
// Сначала мы ищем хотя бы один курс с обновленным timestamp, и если находим, то начинаем прослушивать
// все курсы данного преподавателя
    fun findByYourAsTeacher(): Flow<List<CourseInfo>> {
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
            .map { courseMapper.entityToDomainInfo2(it) }
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
                        val removedCourseContents = courseContents.filter { it.deleted }
                        val remainingCourseContent = courseContents - removedCourseContents.toSet()

                        courseContentDao.upsert(remainingCourseContent)

                        courseContentDao.delete(removedCourseContents)

                        removedCourseContents.forEach {
                            attachmentStorage.deleteFromLocal(it.id)
                        }

                        coursePreference.setTimestampContentsOfCourse(
                            courseId,
                            courseContents.maxOf { it.timestamp }.time
                        )
                    }
                }
            }
        }

        return sectionDao.getByCourseId(courseId)
            .combine(courseContentDao.getByCourseUuid(courseId)) { sectionEntities, courseContentEntities ->
                CourseContents.sort(
                    courseContentMapper.entityToDomain(courseContentEntities),
                    sectionMapper.entityToDomain(sectionEntities)
                )

                //todo отсортировать секции с курсами
            }
    }

    private fun saveCourseOfTeacher(courseDocs: List<CourseDoc>, teacherUuid: String) {
//        database.runInTransaction {
        externalScope.launch(dispatcher) {
            saveCourses(courseDocs)
            deleteMissingCoursesOfTeacher(courseDocs, teacherUuid)
        }
//        }
    }

    private fun deleteMissingCoursesOfTeacher(courseDocs: List<CourseDoc>, teacherUuid: String) {
        courseDao.deleteMissingByTeacher(courseDocs.map(CourseDoc::id), teacherUuid)
        subjectDao.deleteUnrelatedByCourse()
        groupDao.deleteUnrelatedByCourse()
    }

    private fun saveCoursesOfGroup(courseDocs: List<CourseDoc>, groupUuid: String) {
//        database.runInTransaction {
        externalScope.launch(dispatcher) {
            saveCourses(courseDocs)
            deleteMissingCoursesOfGroup(courseDocs, groupUuid)
        }
//        }
    }

    private fun deleteMissingCoursesOfGroup(courseDocs: List<CourseDoc>, groupUuid: String) {
        groupCourseDao.deleteMissingByGroup(courseDocs.map(CourseDoc::id), groupUuid)
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
                .map { groupUuid ->
                    GroupCourseCrossRef(groupUuid, courseDoc.id)
                }
        }

        userDao.upsert(teacherEntities)
        subjectDao.upsert(subjectEntities)
        courseDao.upsert(courseEntities)
        sectionDao.upsert(courseDocs.flatMap { sectionMapper.docToEntity(it.sections) })
        groupCourseDao.upsert(groupWithCourseEntities)
    }

    fun findByGroupUuid(groupUuid: String): Flow<List<CourseInfo>> {
        if (groupUuid != groupPreference.groupId)
            getCoursesByGroupUuidRemotely(groupUuid)
        return courseDao.getCoursesByGroupId(groupUuid).asFlow()
            .map { courseMapper.entityToDomainInfo(it) }
    }

    private fun getCoursesByGroupUuidRemotely(groupUuid: String) {
        externalScope.launch(dispatcher) {
            coursesByGroupUuidQuery(groupUuid).get().await().let {
                saveCoursesOfGroup(
                    it.toObjects(CourseDoc::class.java),
                    groupUuid
                )
            }
        }
    }

    fun findByTeacherUuid(teacherUuid: String): Flow<List<CourseInfo>> {
        if (teacherUuid != userPreference.id)
            getCoursesByTeacherRemotely(teacherUuid)
        return courseDao.getByTeacherId(teacherUuid)
            .map { courseMapper.entityToDomainInfo2(it) }
    }

    private fun getCoursesByTeacherRemotely(teacherUuid: String) {
        externalScope.launch(dispatcher) {
            coursesByTeacherUuidQuery(teacherUuid).get().await().let {
                saveCourseOfTeacher(
                    it.toObjects(CourseDoc::class.java),
                    teacherUuid
                )
            }
        }
    }


    fun findByYouGroup(): LiveData<List<CourseInfo>> {
        return Transformations.map(
            courseDao.getCoursesByGroupId(groupPreference.groupId)
        ) { entity: List<CourseWithSubjectWithTeacherAndGroups> ->
            courseMapper.entityToDomainInfo(entity)
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
            batch.set(coursesRef.document(courseDoc.id), courseDoc)

            val oldCourse = courseMapper.entityToDomain(courseDao.getSync(course.info.id))
            val oldCourseDoc = courseMapper.domainToDoc(oldCourse)

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
            .map { groupUuid -> addCourseToGroup(groupUuid, courseDoc) }
            .map { it.first to it.second }
            .toMap()
    }

    private suspend fun addCourseToGroup(
        groupUuid: String,
        courseDoc: CourseDoc
    ): Pair<DocumentReference, Map<String, Any>> {
        val groupDoc = groupsRef.document(groupUuid)
            .get()
            .await().toObject(GroupDoc::class.java)!!
        return groupsRef.document(groupUuid) to mapOfAddedFieldsCourse(
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
        val map = courseDoc.groupIds.map { groupUuid ->
            if (updatedSubjectOrTeacherInCourse) {
                updateCourseToGroup(groupUuid, oldCourseDoc, courseDoc)
            } else {
                groupsRef.document(groupUuid) to mutableMapOf<String, Any>(
                    timestampFiledPair(),
                    "timestampCourses" to FieldValue.serverTimestamp()
                )
            }
        }
        return map.map { it.first to it.second }
            .toMap()
    }

    private suspend fun updateCourseToGroup(
        groupUuid: String,
        oldCourseDoc: CourseDoc,
        courseDoc: CourseDoc
    ): Pair<DocumentReference, Map<String, Any>> {
        val groupDoc = groupsRef.document(groupUuid)
            .get()
            .await().toObject(GroupDoc::class.java)!!

        val oldCourseField = SubjectTeacherPair(
            oldCourseDoc.subject.id,
            oldCourseDoc.teacher.id
        )
        return groupsRef.document(groupUuid) to
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
        val removedGroupUuids = oldCourseDoc.groupIds.minus(courseDoc.groupIds)
        return if (removedGroupUuids.isNotEmpty())
            removedGroupUuids.map { groupUuid ->
                removeCourseToGroup(groupUuid, oldCourseDoc)
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
            .map { groupUuid -> removeCourseToGroup(groupUuid, oldCourseDoc) }
            .map { it.first to it.second }
            .toMap()

    }

    private suspend fun removeCourseToGroup(
        groupUuid: String,
        oldCourseDoc: CourseDoc
    ): Pair<DocumentReference, Map<String, Any>> {
        val groupDoc = groupsRef.document(groupUuid)
            .get()
            .await()
            .toObject(GroupDoc::class.java)!!
        return groupsRef.document(groupUuid) to mapOfDeletedCourseFields(
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
        groupsUuids: List<String>,
        batch: WriteBatch
    ) {
        for (groupUuid in groupsUuids) {
            batch.update(
                groupsRef.document(groupUuid),
                mapOf(
                    timestampFiledPair(),
                    "timestampCourses" to FieldValue.serverTimestamp()
                )
            )
        }
    }

    private fun timestampFiledPair() = "timestamp" to FieldValue.serverTimestamp()


    fun findByTypedName(name: String): Flow<Resource2<List<CourseInfo>>> = callbackFlow {
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

    fun findWhereYouTeacher(): Flow<List<CourseInfo>> {
        return courseDao.getByTeacherId(userPreference.id)
            .map { courseMapper.entityToDomainInfo2(it) }
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
                attachmentStorage.get(contentId, ListConverter.tolList(attachments))
            }
    }

    suspend fun addTask(task: Task) {
        val attachments = attachmentStorage.addContentAttachments(task.id, task.attachments)
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
        val attachments = attachmentStorage.update(task.id, task.attachments)
        val cacheTask = courseContentMapper.entityToTaskDomain(courseContentDao.getSync(task.id))
        val updatedFields = MembersComparator.mapOfDifference(cacheTask, task)
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
        attachmentStorage.deleteFilesByContentId(courseContent.id)
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

}

class SameCoursesException : Exception()