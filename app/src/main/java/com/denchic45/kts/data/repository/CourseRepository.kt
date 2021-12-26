package com.denchic45.kts.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.asFlow
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.domain.Course
import com.denchic45.kts.data.model.domain.CourseInfo
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.firestore.SubjectTeacherPair
import com.denchic45.kts.data.model.firestore.TaskDoc
import com.denchic45.kts.data.model.mapper.*
import com.denchic45.kts.data.model.room.CourseWithSubjectWithTeacherAndGroups
import com.denchic45.kts.data.model.room.GroupCourseCrossRef
import com.denchic45.kts.data.prefs.AppPreference
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.TimestampPreference
import com.denchic45.kts.data.prefs.TimestampPreference.Companion.TIMESTAMP_LAST_UPDATE_GROUP_COURSES
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.di.modules.IoDispatcher
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
    private val userMapper: UserMapper,
    private val externalScope: CoroutineScope,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val courseMapper: CourseMapper,
    private val groupPreference: GroupPreference,
    private val subjectMapper: SubjectMapper,
    override val networkService: NetworkService,
    private val firestore: FirebaseFirestore,
    private val database: DataBase,
    private val userPreference: UserPreference,
    private val timestampPreference: TimestampPreference,
    private val appPreference: AppPreference,
    private val groupMapper: GroupMapper,
    private val specialtyMapper: SpecialtyMapper,
    private val sectionMapper: SectionMapper,
    private val taskMapper: TaskMapper,
    private val courseDao: CourseDao,
    private val sectionDao: SectionDao,
    private val groupCourseDao: GroupCourseDao,
    private val subjectDao: SubjectDao,
    private val groupDao: GroupDao,
    private val userDao: UserDao,
    private val specialtyDao: SpecialtyDao,
    private val taskDao: TaskDao
) : Repository(context) {
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val coursesRef: CollectionReference = firestore.collection("Courses")

//    private var startDrop = 1

    fun find(courseUuid: String): Flow<Course> {
        addListenerRegistration("findCourseByUuid $courseUuid") {
            coursesRef.document(courseUuid).addSnapshotListener { value, error ->
                externalScope.launch(dispatcher) {
                    value?.let {
                        if (!value.exists()) {
                            courseDao.deleteByUuid(courseUuid)
                            return@launch
                        }
                        val courseDoc = it.toObject(CourseDoc::class.java)!!
                        database.runInTransaction {
                            launch {

                                groupCourseDao.deleteByCourse(courseDoc.uuid)

                                subjectDao.upsert(
                                    subjectMapper.docToEntity(
                                        courseDoc.subject
                                    )
                                )
                                userDao.upsert(userMapper.docToEntity(courseDoc.teacher))
                                courseDao.upsert(courseMapper.docToEntity(courseDoc))

                                courseDoc.groupUuids?.let { groupUuids ->
                                    if (groupUuids.isNotEmpty()) {
                                        val groupDocs = groupsRef.whereIn("uuid", groupUuids)
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
                                                GroupCourseCrossRef(
                                                    groupUuid,
                                                    courseDoc.uuid
                                                )
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
        return courseDao.getByUuid(courseUuid)
            .map { courseMapper.entityToDomain(it) }
            .distinctUntilChanged()
    }

    private fun coursesByGroupUuidQuery(groupUuid: String): Query {
        return coursesRef.whereArrayContains("groupUuids", groupUuid)
    }

    private fun coursesByTeacherUuidQuery(teacherUuid: String): Query {
        return coursesRef.whereEqualTo("teacher.uuid", teacherUuid)
    }


    suspend fun observeByYouGroup() {
        timestampPreference.observeValue(TIMESTAMP_LAST_UPDATE_GROUP_COURSES, 0L)
            .filter {it != 0L}
            .drop(if (appPreference.coursesLoadedFirstTime) 1 else 0)
//            .flatMapLatest { timestampPreference.observeValue(TIMESTAMP_LAST_UPDATE_GROUP_COURSES, 0L) }
            .collect {
                appPreference.coursesLoadedFirstTime = true
                getCoursesByGroupUuidRemotely(groupPreference.groupUuid)
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
                        getCoursesByTeacherRemotely(userPreference.uuid)
                    }
                }
        }
        return courseDao.getByTeacherUuid(userPreference.uuid)
            .map { courseMapper.entityToDomainInfo2(it) }
    }

    fun findContentByCourseUuid(courseUuid: String): Flow<List<DomainModel>> {
        coursesRef.document(courseUuid).collection("Contents")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                if (value != null && !value.isEmpty) {
                    taskMapper.docToEntity(value.toObjects(TaskDoc::class.java))
                }
            }

        return sectionDao.getByCourseUuid(courseUuid)
            .combine(taskDao.getByCourseUuid(courseUuid)) { sections, tasks ->
                sectionMapper.entityToDomain(sections) + taskMapper.entityToDomain(tasks)
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
        courseDao.deleteMissingByTeacher(courseDocs.map(CourseDoc::uuid), teacherUuid)
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
        groupCourseDao.deleteMissingByGroup(courseDocs.map(CourseDoc::uuid), groupUuid)
        courseDao.deleteUnrelatedByGroup()
        subjectDao.deleteUnrelatedByCourse()
        userDao.deleteUnrelatedTeachersByCourseOrGroupAsCurator()
    }

    private suspend fun saveCourses(courseDocs: List<CourseDoc>) {
        val courseEntities = courseMapper.docToEntity(courseDocs)
        val teacherEntities = courseDocs.map { userMapper.docToEntity(it.teacher) }
        val subjectEntities = courseDocs.map { subjectMapper.docToEntity(it.subject) }
        val groupWithCourseEntities = courseDocs.flatMap { courseDoc ->
            courseDoc.groupUuids!!
                .filter { groupDao.isExistSync(it) }
                .map { groupUuid ->
                    GroupCourseCrossRef(groupUuid, courseDoc.uuid)
                }
        }
        userDao.upsert(teacherEntities)
        subjectDao.upsert(subjectEntities)
        courseDao.upsert(courseEntities)
        groupCourseDao.upsert(groupWithCourseEntities)
    }

    fun findByGroupUuid(groupUuid: String): Flow<List<CourseInfo>> {
        if (groupUuid != groupPreference.groupUuid)
            getCoursesByGroupUuidRemotely(groupUuid)
        return courseDao.getCoursesByGroupUuid(groupUuid).asFlow()
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
        if (teacherUuid != userPreference.uuid)
            getCoursesByTeacherRemotely(teacherUuid)
        return courseDao.getByTeacherUuid(teacherUuid)
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
            courseDao.getCoursesByGroupUuid(groupPreference.groupUuid)
        ) { entity: List<CourseWithSubjectWithTeacherAndGroups> ->
            courseMapper.entityToDomainInfo(entity)
        }
    }

    suspend fun add(course: Course) {
        checkInternetConnection()
        val courseDoc = courseMapper.domainToDoc(course)
        val refsWithUpdatedFields = addCoursesToGroups(courseDoc)

        val batch = firestore.batch()
        batch.set(coursesRef.document(courseDoc.uuid), courseDoc)
        refsWithUpdatedFields.forEach { batch.update(it.key, it.value) }
        externalScope.launch(dispatcher) { batch.commit().await() }
    }

    suspend fun update(course: Course) {
        checkInternetConnection()
        externalScope.launch(dispatcher) {
            val batch = firestore.batch()
            val courseDoc = courseMapper.domainToDoc(course)
            batch.set(coursesRef.document(courseDoc.uuid), courseDoc)

            val oldCourse = courseMapper.entityToDomain(courseDao.getSync(course.info.uuid))
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
        return courseDoc.groupUuids!!
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
            courseDoc.subject!!.uuid,
            courseDoc.teacher!!.uuid
        )
        val groupHasNotContainCourse = !groupDoc.courses!!.contains(courseField)
        if (groupHasNotContainCourse) {
            return mapOf(
                timestammpFiledPair(),
                "timestampCourses" to FieldValue.serverTimestamp(),
                "courses" to FieldValue.arrayUnion(courseField),
                "subjects.${courseDoc.subject!!.uuid}" to courseDoc.subject!!,
                "teachers.${courseDoc.teacher!!.uuid}" to courseDoc.teacher!!,

                "teacherIds" to FieldValue.arrayUnion(courseDoc.teacher!!.uuid)
            )
        } else {
            throw SameCoursesException()
        }
    }

    private suspend fun updateCourseToGroups(
        oldCourseDoc: CourseDoc, courseDoc: CourseDoc
    ): Map<DocumentReference, Map<String, Any>> {
        val updatedSubjectOrTeacherInCourse = !oldCourseDoc.equalSubjectsAndTeachers(courseDoc)
        val map = courseDoc.groupUuids!!.map { groupUuid ->
            if (updatedSubjectOrTeacherInCourse) {
                updateCourseToGroup(groupUuid, oldCourseDoc, courseDoc)
            } else {
                groupsRef.document(groupUuid) to mutableMapOf<String, Any>(
                    timestammpFiledPair(),
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
            oldCourseDoc.subject!!.uuid,
            oldCourseDoc.teacher!!.uuid
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
        val removedGroupUuids = oldCourseDoc.groupUuids!!.minus(courseDoc.groupUuids!!)
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
        batch.delete(coursesRef.document(courseDoc.uuid))
        val refsWithUpdatedFields = removeCoursesToGroups(courseDoc)

        refsWithUpdatedFields.forEach {
            batch.update(it.key, it.value)
        }
        externalScope.launch(dispatcher) { batch.commit().await() }
    }

    private suspend fun removeCoursesToGroups(
        oldCourseDoc: CourseDoc
    ): Map<DocumentReference, Map<String, Any>> {
        return oldCourseDoc.groupUuids!!
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
            oldCourseDoc.subject!!.uuid,
            oldCourseDoc.teacher!!.uuid
        )

        val updatedFieldsGroup = mutableMapOf<String, Any>(
            timestammpFiledPair(),
            "timestampCourses" to FieldValue.serverTimestamp()
        )

        updatedFieldsGroup["courses"] = FieldValue.arrayRemove(oldCourseField)
        // remove if not use
        val groupCoursesWithoutOldCourse = groupDoc.courses!!.toMutableList()
        groupCoursesWithoutOldCourse.removeIf { courseFieldInDoc -> courseFieldInDoc == oldCourseField }
        val subjectInOldCourseNoLongerUsed =
            groupCoursesWithoutOldCourse.none { courseFieldInDoc ->
                courseFieldInDoc.subjectUuid == oldCourseField.subjectUuid
            }
        val teacherInOldCourseNoLongerUsed =
            groupCoursesWithoutOldCourse.none { courseFieldInDoc ->
                courseFieldInDoc.teacherUuid == oldCourseField.teacherUuid
            }
        if (subjectInOldCourseNoLongerUsed) {
            updatedFieldsGroup["subjects.${oldCourseDoc.subject!!.uuid}"] = FieldValue.delete()
        }
        if (teacherInOldCourseNoLongerUsed) {
            updatedFieldsGroup["teachers.${oldCourseDoc.teacher!!.uuid}"] = FieldValue.delete()
            updatedFieldsGroup["teacherIds"] = FieldValue.arrayRemove(oldCourseDoc.teacher!!.uuid)
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
                    timestammpFiledPair(),
                    "timestampCourses" to FieldValue.serverTimestamp()
                )
            )
        }
    }

    private fun timestammpFiledPair() = "timestamp" to FieldValue.serverTimestamp()


    fun findByTypedName(name: String): Flow<Resource<List<CourseInfo>>> = callbackFlow {
        addListenerRegistration("name") {
            coursesRef
                .whereArrayContains("searchKeys", name.lowercase())
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    val courseDocs = value!!.toObjects(CourseDoc::class.java)
                    externalScope.launch(dispatcher) { saveCourses(courseDocs) }
//
                    trySend(
                        Resource.successful(
                            courseMapper.docToDomain(courseDocs)
                        )
                    )
                }
        }
        awaitClose { }
    }

    fun findWhereYouTeacher(): Flow<List<CourseInfo>> {
        return courseDao.getByTeacherUuid(userPreference.uuid)
            .map { courseMapper.entityToDomainInfo2(it) }
    }

    suspend fun removeGroup(group: Group) {
        val courseDocs = coursesRef.whereArrayContains("groupUuids", group.uuid)
            .get()
            .await()
            .toObjects(CourseDoc::class.java)
        val batch = firestore.batch()
        courseDocs.forEach { courseDoc ->
            batch.update(
                coursesRef.document(courseDoc.uuid),
                mapOf(
                    timestammpFiledPair(),
                    "groupUuids" to FieldValue.arrayRemove(group.uuid)
                )
            )
        }
        batch.commit().await()
    }
}

class SameCoursesException : Exception()