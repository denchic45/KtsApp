package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.room.withTransaction
import com.denchic45.appVersion.AppVersionService
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.database.DataBase
import com.denchic45.kts.data.getQuerySnapshotFlow
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.domain.User.Companion.isStudent
import com.denchic45.kts.data.model.firestore.CourseDoc
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.*
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.TimestampPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.SearchKeysGenerator
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
    override val context: Context,
    override val appVersionService: AppVersionService,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    override val courseDao: CourseDao,
    override val userDao: UserDao,
    override val groupDao: GroupDao,
    override val specialtyDao: SpecialtyDao,
    private val timestampPreference: TimestampPreference,
    private val groupPreference: GroupPreference,
    override val networkService: NetworkService,
    private val userPreference: UserPreference,
    override val userMapper: UserMapper,
    override val groupMapper: GroupMapper,
    private val groupMemberMapper: GroupMemberMapper,
    override val courseMapper: CourseMapper,
    override val specialtyMapper: SpecialtyMapper,
    override val sectionMapper: SectionMapper,
    override val subjectMapper: SubjectMapper,
    override val groupCourseDao: GroupCourseDao,
    override val sectionDao: SectionDao,
    override val subjectDao: SubjectDao,
    private val firestore: FirebaseFirestore,
    override val dataBase: DataBase
) : Repository(context), SaveGroupOperation, SaveCourseRepository,
    FindByContainsNameRepository<GroupHeader> {

    override fun findByContainsName(text: String): Flow<List<GroupHeader>> {
        return groupsRef
            .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(text))
            .getQuerySnapshotFlow()
            .filter { timestampsNotNull(it) }
            .map {
                it.toObjects(GroupDoc::class.java).let { groupDocs ->
                    for (groupDoc in groupDocs) {
                        saveGroup(groupDoc)
                    }
                    groupMapper.docToCourseGroupDomain(groupDocs)
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
        groupPreference.saveGroupInfo(groupMapper.docToEntity(groupDoc))
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
        groupsRef.whereEqualTo("curator.id", userPreference.id)
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
        val queryGroup: Query = if (isStudent(User.Role.valueOf(userPreference.role))) {
            getQueryOfGroupById(userPreference.groupId)
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
                groupMapper.docToCourseGroupDomain(toObjects(GroupDoc::class.java))
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
        get() = groupPreference.groupId

    val yourGroupName: String
        get() = groupPreference.groupName

    private fun getQueryOfGroupById(ud: String): Query {
        return groupsRef.whereEqualTo("id", ud)
    }

    private val queryOfYourGroupByCurator: Query
        get() = groupsRef.whereEqualTo("curator.id", userPreference.id)

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
        return groupDao.observe(groupId)
            .map {
                it?.let { groupMapper.entityToDomain(it) }
            }
            .distinctUntilChanged()
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
                            groupDao.deleteById(groupId)
                        }
                    }
                }
        }
    }


    suspend fun add(group: Group) {
        requireAllowWriteData()
        val groupDoc = groupMapper.domainToDoc(group)
        groupDocReference(groupDoc.id).set(groupDoc, SetOptions.merge())
            .await()
    }

    suspend fun update(group: Group) {
        requireAllowWriteData()
        val updatedGroupMap = groupMapper.domainToMap(group)
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
        updatedGroupMap["curator"] = userMapper.domainToDoc(teacher)
        updatedGroupMap["timestamp"] = FieldValue.serverTimestamp()
        groupDocReference(groupId).update(updatedGroupMap)
            .await()
    }

    fun findCurator(groupId: String): Flow<User?> {
        return userDao.observeCurator(groupId).map {
            it?.let { userMapper.entityToDomain(it) }
        }
    }

    fun getNameByGroupId(groupId: String): Flow<String> {
        coroutineScope.launch(dispatcher) {
            if (!groupDao.isExist(groupId)) {
                getGroupByIdRemotely(groupId)
            }
        }
        return groupDao.getNameById(groupId)
            .filterNotNull()
            .distinctUntilChanged()
    }

    fun isExistGroup(groupId: String): Flow<Boolean> {
        return groupDao.observeIsExist(groupId)
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
                groupMapper.docToCourseGroupDomain(groupDoc),
                courseMapper.docToDomain2(
                    courseDocs.filter { it.groupIds.contains(groupDoc.id) }
                )
            )
        }
        return groupsInfo
    }

    fun observeHasGroup(): Flow<Boolean> {
        return groupPreference.observeValue(GroupPreference.GROUP_ID, "")
            .map(String::isNotEmpty)
    }

    fun findGroupByStudent(user: User): Flow<Group> = flow {
        if (!userDao.isExistByIdAndGroupId(user.id, user.groupId))
            findGroupById(user.groupId!!)

        emitAll(
            groupDao.getByStudentId(user.id)
                .map { groupMapper.entityToDomain(it) }
        )
    }

    fun findGroupByCurator(user: User): Flow<Group> {
        getQueryOfGroupByCurator(user.id)
            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    throw error
                }
                if (!value!!.isEmpty && timestampsNotNull(value))
                    coroutineScope.launch(dispatcher) {
                        saveGroup(
                            value.documents[0].toObject(GroupDoc::class.java)!!
                        )
                    }
            }
        return groupDao.getByCuratorId(user.id)
            .map { domain: GroupWithCuratorAndSpecialtyEntity ->
                groupMapper.entityToDomain(domain)
            }
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
        return userDao.observeStudentsWithCuratorByGroupId(groupId)
            .filterNotNull()
            .map { groupMemberMapper.entityToDomainGroupMembers(it) }
    }

    suspend fun setHeadman(studentId: String, groupId: String) {
        groupsRef.document(groupId).update("headmanId", studentId).await()
    }

    suspend fun removeHeadman(groupId: String) {
        groupsRef.document(groupId).update("headmanId", null).await()
    }
}