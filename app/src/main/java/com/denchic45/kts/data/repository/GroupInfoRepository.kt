package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.room.withTransaction
import com.denchic45.kts.data.DataBase
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.dao.CourseDao
import com.denchic45.kts.data.dao.GroupDao
import com.denchic45.kts.data.dao.SpecialtyDao
import com.denchic45.kts.data.dao.UserDao
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.GroupCourses
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.domain.User.Companion.isStudent
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.mapper.CourseMapper
import com.denchic45.kts.data.model.mapper.GroupMapper
import com.denchic45.kts.data.model.mapper.SpecialtyMapper
import com.denchic45.kts.data.model.mapper.UserMapper
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
    private val firestore: FirebaseFirestore,
    override val dataBase: DataBase
) : Repository(context), IGroupRepository {

    private val compositeDisposable = CompositeDisposable()
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val usersRef: CollectionReference = firestore.collection("Users")

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
                        val groupDocs = snapshots.toObjects(
                            GroupDoc::class.java
                        )
                        saveUsersAndGroupsAndSubjectsOfTeacher(groupDocs, teacherId)
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
                    upsertGroupInfo(groupDoc)
                }
            }
            .addOnFailureListener { e: Exception? -> Log.d("lol", "loadGroupInfo: ", e) }
    }

    private suspend fun upsertGroupInfo(groupDoc: GroupDoc) {
        groupDao.upsert(groupMapper.docToEntity(groupDoc))
        specialtyDao.upsert(specialtyMapper.docToEntity(groupDoc.specialty))
        saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(groupDoc)
    }

    fun hasGroup(): Boolean {
        return groupPreference.groupId.isNotEmpty()
    }

    fun find(groupId: String): LiveData<Group> {
        getGroupByIdRemotely(groupId)
        return Transformations.map(groupDao.get(groupId)) { domain: GroupWithCuratorAndSpecialtyEntity ->
            groupMapper.entityToDomain(domain)
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
                                upsertGroupInfo(groupDoc)
                            }
                        } else {
                            groupDao.deleteById(groupId)
                        }
                    }
                }
        }
    }

    fun findByTypedName(name: String): Flow<Resource<List<CourseGroup>>> = callbackFlow {
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
                                saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(groupDoc)
                            }
                            trySend(Resource.Success(groupMapper.docToCourseGroupDomain(groupDocs)))
                        }
                    }
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
            groupsRef.document(groupDoc.id).set(groupDoc, SetOptions.merge())
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
            groupsRef.document(group.id).update(updatedGroupMap)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    suspend fun remove(group: Group) {
        checkInternetConnection()
        val batch = firestore.batch()
        val task = groupsRef.document(group.id)
            .get()
            .await()
        batch.delete(groupsRef.document(group.id))
        task.toObject(GroupDoc::class.java)!!.students!!.values
            .forEach(Consumer { (id) ->
                batch.delete(usersRef.document(id))
            })
        batch.commit().await()
    }

    fun updateGroupCurator(groupId: String, teacher: User): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(NetworkException())
                return@create
            }
            val updatedGroupMap: MutableMap<String, Any> = HashMap()
            updatedGroupMap["curator"] = userMapper.domainToDoc(teacher)
            updatedGroupMap["timestamp"] = FieldValue.serverTimestamp()
            groupsRef.document(groupId).update(updatedGroupMap)
                .addOnSuccessListener { emitter.onComplete() }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    fun findCurator(groupId: String): LiveData<User> {
        return Transformations.map(userDao.getCurator(groupId)) { entity: UserEntity? ->
            userMapper.entityToDomain(entity)
        }
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

    fun findGroupsWithCoursesByCourse(course: Int): Single<List<GroupCourses>> {
        return Single.create { emitter: SingleEmitter<List<GroupCourses>> ->
            groupsRef
                .whereEqualTo("course", course)
                .get()
                .addOnSuccessListener { snapshots: QuerySnapshot ->
                    val groupDocs = snapshots.toObjects(GroupDoc::class.java)
                    val groupsInfo: MutableList<GroupCourses> = ArrayList()
                    coroutineScope.launch(dispatcher) {
                        groupDocs.forEach(Consumer { groupDoc: GroupDoc ->
                            launch {
                                upsertGroupInfo(groupDoc)
                                groupsInfo.add(
                                    GroupCourses(
                                        groupMapper.docToCourseGroupDomain(groupDoc),
                                        courseMapper.entityToDomain2(
                                            courseDao.getCoursesByGroupIdSync(groupDoc.id)
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
        return groupPreference.observeValue(GroupPreference.GROUP_ID, "")
            .map(String::isNotEmpty)
    }

    fun findGroupByStudent(user: User): Observable<Group> {
        return Observable.create { emitter: ObservableEmitter<Group> ->
            if (!userDao.isExistByIdAndGroupId(user.id, user.groupId)) findGroupById(
                user.groupId!!,
                emitter
            )
            addListenerDisposable("GROUP_BY_STUDENT: " + user.id) {
                groupDao.getByStudentId(user.id)
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
            getQueryOfGroupByCurator(user.id)
                .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        emitter.onError(error)
                        return@addSnapshotListener
                    }
                    if (!value!!.isEmpty && timestampsNotNull(value))
                        coroutineScope.launch(dispatcher) {
                            saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(
                                value.documents[0].toObject(GroupDoc::class.java)!!
                            )
                        }
                }
            addListenerDisposable("GROUP_BY_CURATOR: " + user.id) {
                groupDao.getByCuratorId(user.id)
                    .map { domain: GroupWithCuratorAndSpecialtyEntity ->
                        groupMapper.entityToDomain(domain)
                    }
                    .subscribe { value: Group -> emitter.onNext(value) }
            }
        }
    }

    private fun findGroupById(groupId: String, emitter: ObservableEmitter<Group>) {
        groupsRef.document(groupId)
            .get()
            .addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                coroutineScope.launch(dispatcher) {
                    saveUsersAndTeachersWithSubjectsAndCoursesOfGroup(
                        documentSnapshot.toObject(GroupDoc::class.java)!!
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