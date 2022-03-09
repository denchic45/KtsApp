package com.denchic45.kts.data.repository

import android.content.Context
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.UserDao
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.firestore.UserDoc
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.data.model.room.UserEntity
import com.denchic45.kts.data.prefs.GroupPreference
import com.denchic45.kts.data.prefs.UserPreference
import com.denchic45.kts.di.modules.IoDispatcher
import com.denchic45.kts.utils.SearchKeysGenerator
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
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

open class UserRepository @Inject constructor(
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val userPreference: UserPreference,
    override val networkService: NetworkService,
    private val groupPreference: GroupPreference,
    private val userDao: UserDao,
    private val userMapper: UserMapper,
    firestore: FirebaseFirestore
) : Repository() {
    private val usersRef: CollectionReference = firestore.collection("Users")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val avatarStorage: StorageReference = storage.reference.child("avatars")

    fun getByGroupId(groupId: String): Flow<List<User>> {
        return userDao.getByGroupId(groupId).map { userMapper.entityToDomain(it) }

    }

    private fun loadUserPreference(user: User) {
        userPreference.id = user.id
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
        user.groupId?.let { groupPreference.groupId = it }
    }

    fun findSelf(): User {
        return User(
            userPreference.id,
            userPreference.firstName,
            userPreference.surName,
            userPreference.patronymic,
            groupPreference.groupId,
            userPreference.role,
            userPreference.phoneNum,
            userPreference.email,
            userPreference.photoUrl,
            Date(userPreference.timestamp), userPreference.gender,
            userPreference.isGeneratedAvatar, userPreference.isAdmin
        )
    }


    suspend fun loadAvatar(avatarBytes: ByteArray, userId: String): String {
        val reference = avatarStorage.child(userId)
        reference.putBytes(avatarBytes).await()
        return reference.downloadUrl.await().toString()
    }

    suspend fun add(user: User) {
        checkInternetConnection()
        val userDoc = userMapper.domainToDoc(user)
        val generator = SearchKeysGenerator()
        userDoc.searchKeys =
            generator.generateKeys(user.fullName) { predicate: String -> predicate.length > 2 }
        usersRef.document(user.id).set(userDoc)
            .await()
    }

    suspend fun update(user: UserEntity, searchKeys: List<String>) {
        checkInternetConnection()
        userDao.update(user)
        val userDoc = userMapper.entityToDoc(user)
        userDoc.searchKeys = searchKeys
        usersRef.document(user.id).set(userDoc)
            .await()

    }

    open suspend fun remove(user: User) {
        checkInternetConnection()
        deleteAvatar(user.id)
        usersRef.document(user.id)
            .delete()
            .await()

    }

    private suspend fun deleteAvatar(userId: String) {
        val reference = avatarStorage.child(userId)
        reference.delete()
            .await()
    }

    fun getByTypedName(name: String): Flow<List<User>> = callbackFlow {
        addListenerRegistration("getByTypedName") {
            usersRef
                .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(name))
                .addSnapshotListener { snapshots: QuerySnapshot?, error: FirebaseFirestoreException? ->
                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }
                    val users = snapshots!!.toObjects(
                        UserDoc::class.java
                    )
                    launch(dispatcher) {
                        userDao.upsert(userMapper.docToEntity(users))
                    }
                    trySend(userMapper.docToDomain(users))
                }
        }
        awaitClose { }
    }

    suspend fun findAndSaveByPhoneNum(phoneNum: String) {
        checkInternetConnection()
        usersRef.whereEqualTo("phoneNum", phoneNum)
            .get()
            .await().apply {
                if (isEmpty) {
                    throw Exception("Не верный номер")
                }
                val user = documents[0].toObject(
                    User::class.java
                )
                loadUserPreference(user!!)
            }
    }

    suspend fun findAndSaveByEmail(email: String) {
        checkInternetConnection()
        usersRef.whereEqualTo("email", email)
            .get()
            .await().apply {
                if (isEmpty) {
                    throw  FirebaseAuthException(
                        "ERROR_USER_NOT_FOUND",
                        "Nothing user!"
                    )
                }
                val user = documents[0].toObject(User::class.java)!!
                loadUserPreference(user)
            }

    }

    val thisUserObserver: Flow<User?> = callbackFlow {
        usersRef.whereEqualTo("id", userPreference.id)
            .addSnapshotListener { value: QuerySnapshot?, error: FirebaseFirestoreException? ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (value!!.isEmpty) {
                    trySend(null)
                    Unit
                } else {
                    val user = value.documents[0].toObject(
                        User::class.java
                    )!!
                    if (user.timestamp != null) {
                        loadUserPreference(user)
                        trySend(user)
                    }
                }
            }
        awaitClose { }
    }

    fun observeById(userId: String): Flow<User?> {
        if (!userDao.isExist(userId)) {
            usersRef.document(userId)
                .addSnapshotListener { value: DocumentSnapshot?, error: FirebaseFirestoreException? ->
                    coroutineScope.launch(dispatcher) {
                        value?.let {
                            userDao.upsert(userMapper.docToEntity(value.toObject(UserDoc::class.java)!!))
                        }
                    }
                }
        }
        return userDao.get(userId)
            .map { entity: UserEntity? -> entity?.let { userMapper.entityToDomain(entity) } }
    }

//    fun getById(id: String): LiveData<User> {
//        if (userDao.get(id) == null) {
//            addListenerRegistration("byId") {
//                usersRef.whereEqualTo("id", id)
//                    .addSnapshotListener { snapshot: QuerySnapshot?, error: FirebaseFirestoreException? ->
//                        if (error != null) {
//                            Log.d("lol", "error: ", error)
//                            return@addSnapshotListener
//                        }
//                        coroutineScope.launch(dispatcher) {
//                            userDao.upsert(snapshot!!.documents[0].toObject(UserEntity::class.java)!!)
//                        }
//                    }
//            }
//        }
//        return Transformations.map(userDao.get(id)) { entity: UserEntity ->
//            userMapper.entityToDomain(entity)
//        }
//    }
}