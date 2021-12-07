package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.dao.*
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.GroupWithCourses
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.domain.User.Companion.isStudent
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.*
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import com.denchic45.kts.data.model.room.UserEntity
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.TimestampPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.NetworkException
import com.denchic45.kts.utils.SearchKeysGenerator
import com.google.firebase.firestore.*
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors
import javax.inject.Inject

class GroupInfoRepository @Inject constructor(
    context: Context,
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
    private val firestore: FirebaseFirestore
) : Repository(context), IGroupRepository {

    private val compositeDisposable = CompositeDisposable()
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val usersRef: CollectionReference = firestore.collection("Users")
//    private var batch: WriteBatch? = null

    private suspend fun saveUsersAndTeachersWithSubjectsAndCoursesOfYourGroup(groupDoc: GroupDoc) {
        saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(groupDoc)
        groupPreference.saveGroupInfo(groupMapper.docToEntity(groupDoc))
        timestampPreference.setTimestampGroupCourses(groupDoc.timestampCourses!!.time)
    }

//    override suspend fun saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(groupDoc: GroupDoc) {
//        upsertUsersOfGroup(groupDoc)
//        groupDao.upsert(groupMapper.docToEntity(groupDoc))
//        specialtyDao.upsert(specialtyMapper.docToEntity(groupDoc.specialty))
//    }

    private suspend fun upsertUsersOfGroup(groupDoc: GroupDoc) {
        val allUsersEntity = userMapper.docToEntity(groupDoc.allUsers)
        userDao.upsert(allUsersEntity)
        val availableUsers = allUsersEntity.stream().map { obj: UserEntity -> obj.uuid }
            .collect(Collectors.toList())
        availableUsers.add(groupDoc.curator!!.uuid)
        userDao.deleteMissingStudentsByGroup(availableUsers, groupDoc.uuid)
    }

    private suspend fun saveUsersAndGroupsAndSubjectsOfTeacher(
        groupDocs: List<GroupDoc>,
        teacherUuid: String
    ) {
        for (groupDoc in groupDocs) {
            upsertUsersOfGroup(groupDoc)
            groupDao.upsert(groupMapper.docToEntity(groupDoc))
            specialtyDao.upsert(specialtyMapper.docToEntity(groupDoc.specialty))
        }
    }

    fun listenYourGroup() {
        addListenerRegistration("yourGroup") { yourGroupByUuidListener }
    }

    fun listenYouGroupByCurator() {
        addListenerRegistration("byCurator") { yourGroupByCuratorListener }
    }

    fun listenGroupsWhereThisUserIsTeacher(teacher: User) {
        addListenerRegistration(teacher.uuid) { getUpdatedGroupsByTeacherListener(teacher) }
    }

    private fun getUpdatedGroupsByTeacherListener(teacher: User): ListenerRegistration {
        val timestampGroups = timestampPreference.lastUpdateGroupsTimestamp
        val teacherUuid = teacher.uuid
        return groupsRef.whereArrayContains("teacherIds", teacherUuid)
            .whereGreaterThan("timestamp", Date(timestampGroups))
            .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    error.printStackTrace()
                    throw error
                }
                if (!snapshots!!.isEmpty) {
                    coroutineScope.launch(dispatcher) {
                        val groupDocs = snapshots.toObjects(
                            GroupDoc::class.java
                        )
                        saveUsersAndGroupsAndSubjectsOfTeacher(groupDocs, teacherUuid)
                        timestampPreference.setTimestampGroups(System.currentTimeMillis())
                    }
                }
            }
    }

    private val yourGroupByCuratorListener: ListenerRegistration
        get() = groupsRef.whereEqualTo("curator.uuid", userPreference.uuid)
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
    private val yourGroupByUuidListener: ListenerRegistration
        get() {
            val queryGroup: Query = if (isStudent(userPreference.role)) {
                val yourGroupUuid = groupPreference.groupUuid
                getQueryOfGroupByUuid(yourGroupUuid)
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

    private fun getQueryOfGroupByUuid(uuid: String): Query {
        return groupsRef.whereEqualTo("uuid", uuid)
    }

    private val queryOfYourGroupByCurator: Query
        get() = groupsRef.whereEqualTo("curator.uuid", userPreference.uuid)

    private fun getQueryOfGroupByCurator(curatorUuid: String): Query {
        return groupsRef.whereEqualTo("curator.uuid", curatorUuid)
    }

    private fun hasTimestamp(groupDoc: GroupDoc): Boolean {
        // May be null due to @ServerTimestamp
        return groupDoc.timestamp != null
    }

    fun getStudentsOfGroupByGroupUuid(groupUuid: String?): LiveData<List<User?>> {
        if (groupDao.getByUuid(groupUuid) == null) {
            findGroupInfoByUuid(groupUuid)
        }
        return Transformations.map(userDao.getStudentsOfGroupByGroupUuid(groupUuid)) { entities: List<UserEntity?> ->
            ArrayList(
                userMapper.entityToDomain(entities)
            )
        }
    }

    fun findGroupInfoByUuid(groupUuid: String?) {
        groupsRef.document(groupUuid!!)
            .get()
            .addOnSuccessListener { snapshot: DocumentSnapshot ->
                val groupDoc = snapshot.toObject(
                    GroupDoc::class.java
                )
                coroutineScope.launch(dispatcher) {
                    upsertGroupInfo(groupDoc)
                }
            }
            .addOnFailureListener { e: Exception? -> Log.d("lol", "loadGroupInfo: ", e) }
    }

    private suspend fun upsertGroupInfo(groupDoc: GroupDoc?) {
        groupDao.upsert(groupMapper.docToEntity(groupDoc))
        specialtyDao.upsert(specialtyMapper.docToEntity(groupDoc!!.specialty))
        saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(groupDoc)
    }

    //    public boolean isGroupHasSuchTeacher(String uuid_user, String groupUuid) {
    //        return courseDao.isGroupHasSuchTeacher(uuid_user, groupUuid);
    //    }
    fun hasGroup(): Boolean {
        return groupPreference.groupUuid.isNotEmpty()
    }

    fun find(groupUuid: String): LiveData<Group> {
        getGroupByUuidRemotely(groupUuid)
        return Transformations.map(groupDao.getByUuid(groupUuid)) { domain: GroupWithCuratorAndSpecialtyEntity ->
            groupMapper.entityToDomain(domain)
        }
    }

    private fun getGroupByUuidRemotely(groupUuid: String) {
        addListenerRegistration(groupUuid) {
            groupsRef.document(groupUuid)
                .addSnapshotListener { snapshot: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                    coroutineScope.launch(dispatcher) {
                        if (snapshot!!.exists()) {
                            val groupDoc = snapshot.toObject(GroupDoc::class.java)
                            if (hasTimestamp(groupDoc!!)) {
                                upsertGroupInfo(groupDoc)
                            }
                        } else {
                            groupDao.deleteByUuid(groupUuid)
                        }
                    }
                }
        }
    }

    fun findByTypedName(name: String): Flow<Resource<List<Group>>> = callbackFlow {
            val registration = groupsRef
                .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(name))
                .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        Log.d("lol", "onError: ", error)
                    }
                    coroutineScope.launch(dispatcher) {
                        val groupDocs = snapshots!!.toObjects(GroupDoc::class.java)
                        for (groupDoc in groupDocs) {
                            saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(groupDoc)
                        }
                        trySend(Resource.successful(groupMapper.docToDomain(groupDocs)))
                    }
                }

        awaitClose {
            registration.remove()
        }
    }


    fun add(group: Group): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            val groupDoc = groupMapper.domainToDoc(group)
            groupsRef.document(groupDoc.uuid).set(groupDoc, SetOptions.merge())
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    fun update(group: Group): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            val updatedGroupMap = groupMapper.domainToMap(group)
            groupsRef.document(group.uuid).update(updatedGroupMap)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    suspend fun remove(group: Group) {
        checkInternetConnection()
        val batch = firestore.batch()
        val task = groupsRef.document(group.uuid)
            .get()
            .await()
        batch.delete(groupsRef.document(group.uuid))
        task.toObject(GroupDoc::class.java)!!.students!!.values
            .forEach(Consumer { (uuid) ->
                batch.delete(
                    usersRef.document(
                        uuid
                    )
                )
            })
        batch.commit().await()
    }

    fun updateGroupCurator(groupUuid: String?, teacher: User?): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            val updatedGroupMap: MutableMap<String, Any> = HashMap()
            updatedGroupMap["curator"] = userMapper.domainToDoc(teacher)
            updatedGroupMap["timestamp"] = FieldValue.serverTimestamp()
            groupsRef.document(groupUuid!!).update(updatedGroupMap)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    fun findCurator(groupUuid: String): LiveData<User> {
        return Transformations.map(userDao.getCurator(groupUuid)) { entity: UserEntity ->
            userMapper.entityToDomain(entity)
        }
    }

    fun getNameByGroupUuid(groupUuid: String): Flow<String> {
        coroutineScope.launch(dispatcher) {
            if (!groupDao.isExistSync(groupUuid)) {
                getGroupByUuidRemotely(groupUuid)
            }
        }
        return groupDao.getNameByUuid(groupUuid)
    }

    fun isExistGroup(groupUuid: String): Flow<Boolean> {
        return groupDao.isExist(groupUuid)
    }

    fun findGroupsWithCoursesByCourse(course: Int): Single<List<GroupWithCourses>> {
        return Single.create { emitter: SingleEmitter<List<GroupWithCourses>> ->
            groupsRef
                .whereEqualTo("course", course)
                .get()
                .addOnSuccessListener { snapshots: QuerySnapshot ->
                    val groupDocs = snapshots.toObjects(GroupDoc::class.java)
                    val groupsInfo: MutableList<GroupWithCourses> = ArrayList()
                    coroutineScope.launch(dispatcher) {
                        groupDocs.forEach(Consumer { groupDoc: GroupDoc ->
                            launch {
                                upsertGroupInfo(groupDoc)
                                groupsInfo.add(
                                    GroupWithCourses(
                                        groupMapper.docToDomain(groupDoc),
                                        courseMapper.entityToDomain(
                                            courseDao.getCoursesByGroupUuidSync(
                                                groupDoc.uuid
                                            )
                                        )
                                    )
                                )
                            }
                        })
                    }.invokeOnCompletion { emitter.onSuccess(groupsInfo) }
                }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    fun observeHasGroup(): Flow<Boolean> {
        return groupPreference.observeValue(GroupPreference.GROUP_UUID, "")
            .map { groupUuid ->
                groupUuid.isNotEmpty()
            }
    }

    fun findGroupByStudent(user: User): Observable<Group> {
        return Observable.create { emitter: ObservableEmitter<Group> ->
            if (!userDao.isExistByUuidAndGroupUuid(user.uuid, user.groupUuid)) findGroupByUuid(
                user,
                emitter
            )
            addListenerDisposable("GROUP_BY_STUDENT: " + user.uuid) {
                groupDao.getByStudentUuid(user.uuid)
                    .subscribe { groupEntity: GroupWithCuratorAndSpecialtyEntity ->
                        emitter.onNext(
                            groupMapper.entityToDomain(groupEntity)
                        )
                    }
            }
        }
    }

    fun findGroupByCurator(user: User): Observable<Group> {
        return Observable.create { emitter: ObservableEmitter<Group> ->
            getQueryOfGroupByCurator(user.uuid)
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        emitter.onError(error)
                        return@addSnapshotListener
                    }
                    if (!value!!.isEmpty)
                        coroutineScope.launch(dispatcher) {
                            saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(
                                value.documents[0].toObject(
                                    GroupDoc::class.java
                                )!!
                            )
                        }
                }
            addListenerDisposable("GROUP_BY_CURATOR: " + user.uuid) {
                groupDao.getByCuratorUuid(user.uuid)
                    .map { domain: GroupWithCuratorAndSpecialtyEntity ->
                        groupMapper.entityToDomain(domain)
                    }
                    .subscribe { value: Group -> emitter.onNext(value) }
            }
        }
    }

    private fun findGroupByUuid(user: User, emitter: ObservableEmitter<Group>) {
        groupsRef.document(user.groupUuid!!)
            .get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                coroutineScope.launch(dispatcher) {
                    saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(
                        documentSnapshot.toObject(
                            GroupDoc::class.java
                        )!!
                    )
                }
            }
            .addOnFailureListener { error: Exception -> emitter.onError(error) }
    }

    override fun removeListeners() {
        super.removeListeners()
        compositeDisposable.clear()
    }
}