package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.dao.UserDao
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.data.model.room.UserEntity
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.SearchKeysGenerator
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.reactivex.rxjava3.core.*
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

open class UserRepository @Inject constructor(
    context: Context,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val userPreference: UserPreference,
    override val networkService: NetworkService,
    private val groupPreference: GroupPreference,
    private val userDao: UserDao,
    private val userMapper: UserMapper,
    firestore: FirebaseFirestore
) : Repository(context) {
    private val usersRef: CollectionReference = firestore.collection("Users")
    private val groupsRef: CollectionReference = firestore.collection("Groups")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val avatarStorage: StorageReference = storage.reference.child("avatars")
    val teachers: LiveData<List<User>>
        get() {
            getUsersByRole(User.TEACHER, User.HEAD_TEACHER)
            return Transformations.map(userDao.allTeachers) { entity: List<UserEntity?> ->
                userMapper.entityToDomain(
                    entity
                )
            }
        }

    fun getUsersByRole(vararg roles: String?) {
        usersRef.whereIn("role", Arrays.asList(*roles))
            .get().addOnSuccessListener { snapshot: QuerySnapshot ->
                val users = snapshot.toObjects(
                    UserEntity::class.java
                )
                coroutineScope.launch(dispatcher) {
                    userDao.upsert(users)
                }
            }
    }

    fun getByGroupUuid(groupUuid: String?): LiveData<List<User>> {
        //todo check null and get group in fireStore
        return Transformations.map(userDao.getByGroupUuid(groupUuid)) { entity: List<UserEntity?> ->
            userMapper.entityToDomain(
                entity
            )
        }
    }

    private fun loadUserPreference(user: User) {
        userPreference.uuid = user.uuid
        userPreference.firstName = user.firstName
        userPreference.patronymic = user.patronymic ?: ""
        userPreference.setSurname(user.surname)
        userPreference.role = user.role
        userPreference.gender = user.gender
        userPreference.photoUrl = user.photoUrl
        userPreference.email = user.email ?: ""
        userPreference.phoneNum = user.phoneNum
        userPreference.isAdmin = user.admin
        userPreference.timestamp = user.timestamp!!.time
        userPreference.isGeneratedAvatar = user.generatedAvatar
        user.groupUuid?.let { groupPreference.groupUuid = it }
    }

    fun findThisUser(): User {
        return User(
            userPreference.uuid,
            userPreference.firstName,
            userPreference.surName,
            userPreference.patronymic,
            groupPreference.groupUuid,
            userPreference.role,
            userPreference.phoneNum,
            userPreference.email,
            userPreference.photoUrl,
            Date(userPreference.timestamp), userPreference.gender,
            userPreference.isGeneratedAvatar, userPreference.isAdmin
        )
    }

    fun saveGroupUuid(groupUuid: String) {
        groupPreference.groupUuid = groupUuid
    }

    fun getByUuid(uuid: String): LiveData<User> {
        if (userDao.getByUuid(uuid) == null) {
            addListenerRegistration("byUuid") {
                usersRef.whereEqualTo("uuid", uuid)
                    .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
                        if (error != null) {
                            Log.d("lol", "error: ", error)
                            return@addSnapshotListener
                        }
                        coroutineScope.launch(dispatcher) {
                            userDao.insert(snapshot!!.documents[0].toObject(UserEntity::class.java)!!)
                        }
                    }
            }
        }
        return Transformations.map(userDao.getByUuid(uuid)) { entity: UserEntity ->
            userMapper.entityToDomain(
                entity
            )
        }
    }

    suspend fun loadAvatar(avatarBytes: ByteArray, userUuid: String): String {
        val reference = avatarStorage.child(userUuid)
        reference.putBytes(avatarBytes).await()
        return reference.downloadUrl.await().toString()
    }

    fun add(user: User): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            val userEntity = userMapper.domainToEntity(user)
            val userDoc = userMapper.entityToDoc(userEntity)
            val generator = SearchKeysGenerator()
            userDoc.searchKeys =
                generator.generateKeys(user.fullName) { predicate: String -> predicate.length > 2 }
            usersRef.document(user.uuid).set(userDoc)
                .addOnSuccessListener { aVoid: Void? -> emitter.onComplete() }
                .addOnFailureListener { t: Exception? -> emitter.onError(t) }
        }
    }

    fun update(user: UserEntity, searchKeys: List<String>): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            userDao.update(user)
            val userDoc = userMapper.entityToDoc(user)
            userDoc.searchKeys = searchKeys
            usersRef.document(user.uuid).set(userDoc)
                .addOnSuccessListener { aVoid: Void -> emitter.onComplete() }
                .addOnFailureListener { t: Exception? -> emitter.onError(t) }
        }
    }

    open fun remove(user: User): Completable? {
        val userEntity = userMapper.domainToEntity(user)
        return Completable.create { emitter: CompletableEmitter ->
            userDao.delete(userEntity)
            deleteAvatar(userEntity.uuid)
            usersRef.document(userEntity.uuid)
                .delete()
                .addOnSuccessListener { aVoid: Void -> emitter.onComplete() }
                .addOnFailureListener { t: Exception -> emitter.onError(t) }
        }
    }

    private fun deleteAvatar(uuid_user: String) {
        val reference = avatarStorage.child(uuid_user)
        reference.delete()
            .addOnSuccessListener { aVoid: Void? -> Log.d("lol", "onSuccess: ") }
            .addOnFailureListener { e: Exception? -> Log.d("lol", "onFailure: ", e) }
    }

    fun getByTypedName(name: String): Flow<Resource<List<User>>> = callbackFlow {
        addListenerRegistration("getByTypedName") {
            usersRef
                .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(name))
                .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val users = snapshots!!.toObjects(
                        User::class.java
                    )
                    launch(dispatcher) {
                        userDao.upsert(userMapper.domainToEntity(users))
                    }
                    trySend(Resource.successful(users))
                }
        }
        awaitClose { }
    }


    fun isExist(uuid: String?): Boolean {
        return userDao.isExist(uuid)
    }

    fun findByPhoneNum(phoneNum: String?): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            if (!networkService.isNetworkAvailable) {
                emitter.onError(FirebaseNetworkException("Отсутствует интернет-соедlинение"))
            }
            usersRef.whereEqualTo("phoneNum", phoneNum)
                .get()
                .addOnSuccessListener { snapshots: QuerySnapshot ->
                    if (snapshots.isEmpty) {
                        emitter.onError(Exception("Не верный номер"))
                        return@addOnSuccessListener
                    }
                    val user = snapshots.documents[0].toObject(
                        User::class.java
                    )
                    loadUserPreference(user!!)
                    emitter.onComplete()
                }
                .addOnFailureListener { t: Exception? -> emitter.onError(t) }
        }
    }

    fun findByEmail(email: String?): Completable {
        return Completable.create { emitter: CompletableEmitter ->
            usersRef.whereEqualTo("email", email)
                .get()
                .addOnSuccessListener { snapshots: QuerySnapshot ->
                    if (snapshots.isEmpty) {
                        emitter.onError(
                            FirebaseAuthException(
                                "ERROR_USER_NOT_FOUND",
                                "Nothing user!"
                            )
                        )
                        return@addOnSuccessListener
                    }
                    val user = snapshots.documents[0].toObject(User::class.java)!!
                    loadUserPreference(user)
                    emitter.onComplete()
                }
                .addOnFailureListener(emitter::onError)
        }
    }

    val thisUserObserver: Flow<Optional<User>> = callbackFlow {

        usersRef.whereEqualTo("uuid", userPreference.uuid)
            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (value!!.isEmpty) {
                    trySend(Optional.empty())
                    Unit
                } else {
                    val user = value.documents[0].toObject(
                        User::class.java
                    )!!
                    if (user.timestamp != null) {
                        loadUserPreference(user)
                        trySend(Optional.of(user))
                    }
                }
            }
        awaitClose { }
    }

    val roleOfThisUser: String
        get() = userPreference.role

    fun findByUuid(uuid: String): Observable<User> {
        return Observable.create { emitter: ObservableEmitter<User> ->
            if (!userDao.isExistByUuidAndGroupUuid(uuid, groupPreference.groupUuid)) {
                usersRef.document(uuid)
                    .addSnapshotListener { value: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                        if (error != null) {
                            emitter.onError(error)
                            return@addSnapshotListener
                        }
                        coroutineScope.launch(dispatcher) {
                            userDao.upsert(value!!.toObject(UserEntity::class.java)!!)
                        }
                    }
            }
            addListenerDisposable("USER_BY_UUID") {
                userDao.getByUuidRx(uuid)
                    .map { entity: UserEntity -> userMapper.entityToDomain(entity) }
                    .subscribe { value: User -> emitter.onNext(value) }
            }
        }
    }
}