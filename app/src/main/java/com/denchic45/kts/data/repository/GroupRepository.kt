package com.denchic45.kts.data.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.room.withTransaction
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.CourseDao
import com.denchic45.kts.data.dao.GroupDao
import com.denchic45.kts.data.dao.SpecialtyDao
import com.denchic45.kts.data.dao.UserDao
import com.denchic45.kts.data.model.domain.*
import com.denchic45.kts.data.model.domain.User.Companion.isStudent
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.CourseMapper
import com.denchic45.kts.data.model.mapper.GroupMapper
import com.denchic45.kts.data.model.mapper.SpecialtyMapper
import com.denchic45.kts.data.model.mapper.UserMapper
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

class GroupRepository @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val courseDao: CourseDao,
    override val userDao: UserDao,
    override val groupDao: GroupDao,
    override val specialtyDao: SpecialtyDao,
    private val timestampPreference: TimestampPreference,
    private val groupPreference: GroupPreference,
    override val networkService: NetworkService,
    private val userPreference: UserPreference,
    override val userMapper: UserMapper,
    override val groupMapper: GroupMapper,
    private val courseMapper: CourseMapper,
    override val specialtyMapper: SpecialtyMapper,
    private val firestore: FirebaseFirestore,
    override val dataBase: DataBase
) : Repository(), SaveGroupOperation {

    private val specialtiesRef: CollectionReference = firestore.collection("Specialties")
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val usersRef: CollectionReference = firestore.collection("Users")
    private val coursesRef: CollectionReference = firestore.collection("Courses")

    private suspend fun saveUsersAndTeachersWithSubjectsAndCoursesOfYourGroup(groupDoc: GroupDoc) {
        saveGroup(groupDoc)
        groupPreference.saveGroupInfo(groupMapper.docToEntity(groupDoc))
        timestampPreference.setTimestampGroupCourses(groupDoc.timestampCourses!!.time)
    }

    fun listenYourGroup() {
        addListenerRegistration("yourGroup") { yourGroupByIdListener }
    }

    fun listenYouGroupByCurator() {
        addListenerRegistration("byCurator") { yourGroupByCuratorListener }
    }

    fun listenGroupsWhereThisUserIsTeacher(teacher: User) {
        addListenerRegistration(teacher.id) { getUpdatedGroupsByTeacherListener(teacher) }
    }

    private fun getUpdatedGroupsByTeacherListener(teacher: User): ListenerRegistration {
        val timestampGroups = timestampPreference.lastUpdateGroupsTimestamp
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

    private val yourGroupByCuratorListener: ListenerRegistration
        get() = groupsRef.whereEqualTo("curator.id", userPreference.id)
            .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.d("lol", "getYourGroupByCuratorListener: ", error)
                    return@addSnapshotListener
                }
                if (!snapshot!!.isEmpty) {
                    coroutineScope.launch(dispatcher) {
                        val groupDoc = snapshot.toObjects(GroupDoc::class.java)[0]
                        if (hasTimestamp(groupDoc)) {
                            saveUsersAndTeachersWithSubjectsAndCoursesOfYourGroup(groupDoc)
                        }
                    }
                }
            }
    private val yourGroupByIdListener: ListenerRegistration
        get() {
            val queryGroup: Query = if (isStudent(userPreference.role)) {
                val yourGroupId = groupPreference.groupId
                getQueryOfGroupById(yourGroupId)
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
                            saveUsersAndTeachersWithSubjectsAndCoursesOfYourGroup(groupDoc)
                        }
                    }
                }
            }
        }


    suspend fun findBySpecialtyId(specialtyId: String): List<CourseGroup> {
        return  groupsRef.whereEqualTo("specialty.id", specialtyId).get()
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
        groupsRef.document(groupId)
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

    fun hasGroup(): Boolean {
        return groupPreference.groupId.isNotEmpty()
    }

    fun find(groupId: String): Flow<Group> {
        getGroupByIdRemotely(groupId)
        return groupDao.get(groupId).map {
            groupMapper.entityToDomain(it)
        }
    }

    private fun getGroupByIdRemotely(groupId: String) {
        addListenerRegistration(groupId) {
            groupsRef.document(groupId)
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

    fun findByTypedName(name: String): Flow<List<CourseGroup>> = callbackFlow {
        val registration = groupsRef
            .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(name))
            .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    Log.d("lol", "onError: ", error)
                }
                coroutineScope.launch(dispatcher) {
                    snapshots?.let {
                        if (timestampsNotNull(snapshots)) {
                            val groupDocs = snapshots.toObjects(GroupDoc::class.java)
                            for (groupDoc in groupDocs) {
                                saveGroup(groupDoc)
                            }
                            trySend(groupMapper.docToCourseGroupDomain(groupDocs))
                        }
                    }
                }
            }

        awaitClose {
            registration.remove()
        }
    }


    suspend fun add(group: Group) {
        checkInternetConnection()
        val groupDoc = groupMapper.domainToDoc(group)
        groupsRef.document(groupDoc.id).set(groupDoc, SetOptions.merge())
            .await()
    }

    suspend fun update(group: Group) {
        checkInternetConnection()
        val updatedGroupMap = groupMapper.domainToMap(group)
        groupsRef.document(group.id).update(updatedGroupMap)
            .await()
    }

    suspend fun remove(group: Group) {
        checkInternetConnection()
        val batch = firestore.batch()

        groupsRef.document(group.id)
            .get()
            .await().apply {
                batch.delete(groupsRef.document(group.id))
                this.toObject(GroupDoc::class.java)!!
                    .students!!
                    .values
                    .forEach { userDoc ->
                        batch.delete(usersRef.document(userDoc.id))
                    }
            }

        coursesRef.whereArrayContains("groupIds", group.id)
            .get()
            .await()
            .forEach { courseDocSnapshot ->
                batch.update(
                    coursesRef.document(courseDocSnapshot.id),
                    "groupIds",
                    FieldValue.arrayRemove(group.id)
                )
            }

        batch.commit().await()
    }

    suspend fun updateGroupCurator(groupId: String, teacher: User) {
        checkInternetConnection()
        val updatedGroupMap: MutableMap<String, Any> = HashMap()
        updatedGroupMap["curator"] = userMapper.domainToDoc(teacher)
        updatedGroupMap["timestamp"] = FieldValue.serverTimestamp()
        groupsRef.document(groupId).update(updatedGroupMap)
            .await()
    }

    fun findCurator(groupId: String): Flow<User> {
        return userDao.getCurator(groupId).map { userMapper.entityToDomain(it) }
    }

    fun getNameByGroupId(groupId: String): Flow<String> {
        coroutineScope.launch(dispatcher) {
            if (!groupDao.isExistSync(groupId)) {
                getGroupByIdRemotely(groupId)
            }
        }
        return groupDao.getNameById(groupId)
    }

    fun isExistGroup(groupId: String): Flow<Boolean> {
        return groupDao.isExist(groupId)
    }

    suspend fun findGroupsWithCoursesByCourse(course: Int): List<GroupCourses> {
        val snapshots = groupsRef
            .whereEqualTo("course", course)
            .get()
            .await()

        val groupDocs = snapshots.toObjects(GroupDoc::class.java)
        val groupsInfo: List<GroupCourses> = groupDocs.map { groupDoc: GroupDoc ->
            saveGroup(groupDoc)
            GroupCourses(
                groupMapper.docToCourseGroupDomain(groupDoc),
                courseMapper.entityToDomain2(
                    courseDao.getCoursesByGroupId(groupDoc.id)
                )
            )
        }
        return groupsInfo
    }

    fun observeHasGroup(): Flow<Boolean> {
        return groupPreference.observeValue(GroupPreference.GROUP_ID, "")
            .map(String::isNotEmpty)
    }

    fun findGroupByStudent(user: User): Flow<Group> {
        if (!userDao.isExistByIdAndGroupId(user.id, user.groupId))
            findGroupById(user.groupId!!)

        return groupDao.getByStudentId(user.id)
            .map { groupMapper.entityToDomain(it) }
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
            val groupDoc = groupsRef.document(groupId)
                .get()
                .await().toObject(GroupDoc::class.java)!!
            saveGroup(groupDoc)
        }
    }
}