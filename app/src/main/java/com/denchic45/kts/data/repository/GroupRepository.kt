package com.denchic45.kts.data.repository

import android.util.Log
import androidx.room.withTransaction
import com.denchic45.kts.data.database.DataBase
import com.denchic45.kts.data.local.db.*
import com.denchic45.kts.data.mapper.*
import com.denchic45.kts.data.model.domain.GroupCourses
import com.denchic45.kts.data.pref.GroupPreferences
import com.denchic45.kts.data.pref.UserPreferences
import com.denchic45.kts.data.prefs.TimestampPreference
import com.denchic45.kts.data.remote.model.CourseDoc
import com.denchic45.kts.data.remote.model.GroupDoc
import com.denchic45.kts.data.service.AppVersionService
import com.denchic45.kts.data.service.NetworkService
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.domain.model.*
import com.denchic45.kts.domain.model.User.Companion.isStudent
import com.denchic45.kts.util.SearchKeysGenerator
import com.denchic45.kts.util.getQuerySnapshotFlow
import com.denchic45.kts.util.timestampsNotNull
import com.google.firebase.firestore.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class GroupRepository @Inject constructor(
    override val appVersionService: AppVersionService,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    override val groupLocalDataSource: GroupLocalDataSource,
    override val specialtyLocalDataSource: SpecialtyLocalDataSource,
    private val timestampPreference: TimestampPreference,
    private val groupPreferences: GroupPreferences,
    private val userPreferences: UserPreferences,
    override val networkService: NetworkService,
    override val groupCourseLocalDataSource: GroupCourseLocalDataSource,
    private val firestore: FirebaseFirestore,
    override val dataBase: DataBase,
    override val userLocalDataSource: UserLocalDataSource,
    override val courseLocalDataSource: CourseLocalDataSource,
    override val sectionLocalDataSource: SectionLocalDataSource,
    override val subjectLocalDataSource: SubjectLocalDataSource
) : Repository(), SaveGroupOperation, SaveCourseRepository,
    FindByContainsNameRepository<GroupHeader> {

    override fun findByContainsName(text: String): Flow<List<GroupHeader>> {
        return groupsRef
            .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(text))
            .getQuerySnapshotFlow()
            .filter { it.timestampsNotNull() }
            .map {
                it.toObjects(GroupDoc::class.java).let { groupDocs ->
                    for (groupDoc in groupDocs) {
                        saveGroup(groupDoc)
                    }
                    groupDocs.docsToGroupHeaders()
                }
            }
    }

    private val specialtiesRef: CollectionReference = firestore.collection("Specialties")
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val usersRef: CollectionReference = firestore.collection("Users")
    private val coursesRef: CollectionReference = firestore.collection("Courses")

    private fun groupDocReference(groupId: String) = groupsRef.document(groupId)

    private suspend fun saveYourGroup(groupDoc: GroupDoc) {
        saveGroup(groupDoc)
        groupPreferences.saveGroupInfo(groupDoc.toEntity())
        timestampPreference.setTimestampGroupCourses(groupDoc.timestampCourses?.time ?: 0)
    }

    fun listenYourGroup() {
        addListenerRegistration("yourGroup") { getYourGroupByIdListener() }
    }

    fun listenYouGroupByCurator() {
        addListenerRegistration("byCurator") { getYourGroupByCuratorListener() }
    }

    fun listenGroupsWhereThisUserIsTeacher(teacher: User) {
        addListenerRegistration(teacher.id) { getUpdatedGroupsByTeacherListener(teacher) }
    }

    private fun getUpdatedGroupsByTeacherListener(teacher: User): ListenerRegistration {
        val timestampGroups = timestampPreference.updateGroupsTimestamp
        val teacherId = teacher.id
        return groupsRef.whereArrayContains("teacherIds", teacherId)
            .whereGreaterThan("timestamp", Date(timestampGroups))
            .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    error.printStackTrace()
                    throw error
                }
                if (!snapshots!!.isEmpty) {
                    coroutineScope.launch(dispatcher) {
                        val groupDocs = snapshots.toObjects(GroupDoc::class.java)
                        dataBase.withTransaction {
                            saveGroups(groupDocs)
                        }
                        timestampPreference.setTimestampGroups(System.currentTimeMillis())
                    }
                }
            }
    }

    private fun getYourGroupByCuratorListener(): ListenerRegistration =
        groupsRef.whereEqualTo("curator.id", userPreferences.id)
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.d("lol", "getYourGroupByCuratorListener: ", error)
                    return@addSnapshotListener
                }
                if (!snapshot!!.isEmpty) {
                    coroutineScope.launch(dispatcher) {
                        val groupDoc = snapshot.toObjects(GroupDoc::class.java)[0]
                        if (hasTimestamp(groupDoc)) {
                            saveYourGroup(groupDoc)
                        }
                    }
                }
            }

    private fun getYourGroupByIdListener(): ListenerRegistration {
        val queryGroup: Query = if (isStudent(User.Role.valueOf(userPreferences.role))) {
            getQueryOfGroupById(userPreferences.groupId)
        } else {
            queryOfYourGroupByCurator
        }
        return queryGroup.addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
            if (error != null) {
                error.printStackTrace()
                throw error
            }
            if (!snapshots!!.isEmpty) {
                coroutineScope.launch(dispatcher) {
                    val groupDoc = snapshots.toObjects(GroupDoc::class.java)[0]
                    if (hasTimestamp(groupDoc)) {
                        saveYourGroup(groupDoc)
                    }
                }
            }
        }
    }


    suspend fun findBySpecialtyId(specialtyId: String): List<GroupHeader> {
        return groupsRef.whereEqualTo("specialty.id", specialtyId).get()
            .await().run {
                toObjects(GroupDoc::class.java).docsToGroupHeaders()
            }
    }

    fun findAllSpecialties(): Flow<List<Specialty>> = callbackFlow {
        val addSnapshotListener =
            specialtiesRef.addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                coroutineScope.launch {
                    snapshot?.let {
                        send(it.toObjects(Specialty::class.java))
                    }
                }
            }

        awaitClose { addSnapshotListener.remove() }
    }

    val yourGroupId: String
        get() = groupPreferences.groupId

    val yourGroupName: String
        get() = groupPreferences.groupName

    private fun getQueryOfGroupById(ud: String): Query {
        return groupsRef.whereEqualTo("id", ud)
    }

    private val queryOfYourGroupByCurator: Query
        get() = groupsRef.whereEqualTo("curator.id", userPreferences.id)

    private fun getQueryOfGroupByCurator(curatorId: String): Query {
        return groupsRef.whereEqualTo("curator.id", curatorId)
    }

    private fun hasTimestamp(groupDoc: GroupDoc): Boolean = groupDoc.timestamp != null

    fun findGroupInfoById(groupId: String) {
        groupDocReference(groupId)
            .get()
            .addOnSuccessListener { snapshot: DocumentSnapshot ->
                val groupDoc = snapshot.toObject(GroupDoc::class.java)!!
                coroutineScope.launch(dispatcher) {
                    dataBase.withTransaction {
                        saveGroup(groupDoc)
                    }
                }
            }
            .addOnFailureListener { e: Exception? -> Log.d("lol", "loadGroupInfo: ", e) }
    }

    fun find(groupId: String): Flow<Group?> {
        getGroupByIdRemotely(groupId)
        return groupLocalDataSource.observe(groupId)
            .distinctUntilChanged()
            .map { it?.toDomain() }
    }

    private fun getGroupByIdRemotely(groupId: String) {
        addListenerRegistration(groupId) {
            groupDocReference(groupId)
                .addSnapshotListener { snapshot: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                    coroutineScope.launch(dispatcher) {
                        if (snapshot!!.exists()) {
                            val groupDoc = snapshot.toObject(GroupDoc::class.java)
                            if (hasTimestamp(groupDoc!!)) {
                                saveGroup(groupDoc)
                            }
                        } else {
                            groupLocalDataSource.deleteById(groupId)
                        }
                    }
                }
        }
    }


    suspend fun add(group: Group) {
        requireAllowWriteData()
        val groupDoc = group.domainToMap()
        groupDocReference(group.id).set(groupDoc, SetOptions.merge())
            .await()
    }

    suspend fun update(group: Group) {
        requireAllowWriteData()
        val updatedGroupMap = group.domainToMap()
        groupDocReference(group.id).update(updatedGroupMap)
            .await()
    }

    suspend fun remove(groupId: String) {
        requireAllowWriteData()
        val batch = firestore.batch()

        groupDocReference(groupId)
            .get()
            .await().apply {
                batch.delete(groupDocReference(groupId))
                this.toObject(GroupDoc::class.java)!!
                    .students!!
                    .values
                    .forEach { userDoc ->
                        batch.delete(usersRef.document(userDoc.id))
                    }
            }

        coursesRef.whereArrayContains("groupIds", groupId)
            .get()
            .await()
            .forEach { courseDocSnapshot ->
                batch.update(
                    coursesRef.document(courseDocSnapshot.id),
                    "groupIds",
                    FieldValue.arrayRemove(groupId)
                )
            }

        batch.commit().await()
    }

    suspend fun updateGroupCurator(groupId: String, teacher: User) {
        requireAllowWriteData()
        val updatedGroupMap: MutableMap<String, Any> = HashMap()
        updatedGroupMap["curator"] = teacher.toDoc()
        updatedGroupMap["timestamp"] = FieldValue.serverTimestamp()
        groupDocReference(groupId).update(updatedGroupMap)
            .await()
    }

    fun getNameByGroupId(groupId: String): Flow<String> {
        coroutineScope.launch(dispatcher) {
            if (!groupLocalDataSource.isExist(groupId)) {
                getGroupByIdRemotely(groupId)
            }
        }
        return groupLocalDataSource.getNameById(groupId)
            .filterNotNull()
            .distinctUntilChanged()
    }

    fun isExistGroup(groupId: String): Flow<Boolean> {
        return groupLocalDataSource.observeIsExist(groupId)
    }

    suspend fun findGroupsWithCoursesByCourse(course: Int): List<GroupCourses> {
        val groupDocs = groupsRef
            .whereEqualTo("course", course)
            .get()
            .await()
            .toObjects(GroupDoc::class.java)

        val courseDocs = coursesRef
            .whereArrayContainsAny("groupIds", groupDocs.map { it.id })
            .get()
            .await()
            .toObjects(CourseDoc::class.java)

        dataBase.withTransaction {
            saveGroups(groupDocs)
            saveCourses(courseDocs)
        }

        val groupsInfo: List<GroupCourses> = groupDocs.map { groupDoc: GroupDoc ->
            GroupCourses(
                groupDoc.toGroupHeader(),

                courseDocs.filter { it.groupIds.contains(groupDoc.id) }.docsToCourseHeaders()
            )
        }
        return groupsInfo
    }

    fun observeHasGroup(): Flow<Boolean> {
        return groupPreferences.observeGroupId.map(String::isNotEmpty)
    }

    fun findGroupByStudent(user: User): Flow<Group> = flow {
        if (!userLocalDataSource.isExistByIdAndGroupId(user.id, user.groupId!!))
            findGroupById(user.groupId!!)

        emitAll(
            groupLocalDataSource.getByStudentId(user.id)
                .map { it.toDomain() }
        )
    }

    fun findGroupByCurator(user: User): Flow<Group> {
        getQueryOfGroupByCurator(user.id)
            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    throw error
                }
                if (!value!!.isEmpty && value.timestampsNotNull())
                    coroutineScope.launch(dispatcher) {
                        saveGroup(
                            value.documents[0].toObject(GroupDoc::class.java)!!
                        )
                    }
            }
        return groupLocalDataSource.getByCuratorId(user.id)
            .map { it.toDomain() }
    }

    private fun findGroupById(groupId: String) {
        coroutineScope.launch(dispatcher) {
            val groupDoc = groupDocReference(groupId)
                .get()
                .await().toObject(GroupDoc::class.java)!!
            saveGroup(groupDoc)
        }
    }

    fun findGroupMembersByGroupId(groupId: String): Flow<GroupMembers> {
        return userLocalDataSource.observeStudentsWithCuratorByGroupId(groupId)
            .filter { it.isNotEmpty() }
            .map { it.toGroupMembers() }
    }

    suspend fun setHeadman(studentId: String, groupId: String) {
        groupsRef.document(groupId).update("headmanId", studentId).await()
    }

    suspend fun removeHeadman(groupId: String) {
        groupsRef.document(groupId).update("headmanId", null).await()
    }
}