package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import com.denchic45.appVersion.AppVersionService
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.dao.UserDao
import com.denchic45.kts.data.getDataFlow
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.firestore.UserDoc
import com.denchic45.kts.data.model.mapper.UserMapper
import com.denchic45.kts.data.model.room.UserEntity
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import javax.inject.Inject

open class UserRepository @Inject constructor(
    override val context: Context,
    override val appVersionService: AppVersionService,
    @IoDispatcher private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope,
    private val userPreference: UserPreference,
    override val networkService: NetworkService,
    private val userDao: UserDao,
    private val userMapper: UserMapper,
    firestore: FirebaseFirestore
) : Repository(context), FindByContainsNameRepository<User> {
    private val usersRef: CollectionReference = firestore.collection("Users")
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val avatarStorage: StorageReference = storage.reference.child("avatars")

    override fun findByContainsName(text: String): Flow<List<User>> {
        return usersRef
            .whereArrayContains("searchKeys", SearchKeysGenerator.formatInput(text))
            .getDataFlow { it.toObjects(UserDoc::class.java) }
            .map { users ->
                userDao.upsert(userMapper.docToEntity(users))
                userMapper.docToDomain(users)
            }
    }

    fun findByGroupId(groupId: String): Flow<List<User>> {
        return userDao.observeByGroupId(groupId).map { userMapper.entityToDomain(it) }
    }

    private fun saveUserPreference(user: User) {
        userPreference.id = user.id
        userPreference.firstName = user.firstName
        userPreference.patronymic = user.patronymic ?: ""
        userPreference.surName = user.surname
        userPreference.role = user.role.toString()
        userPreference.gender = user.gender
        userPreference.photoUrl = user.photoUrl
        userPreference.email = user.email ?: ""
        userPreference.isAdmin = user.admin
        userPreference.timestamp = user.timestamp!!.time
        userPreference.isGeneratedAvatar = user.generatedAvatar
        user.groupId?.let { userPreference.groupId = it }
    }

    fun findSelf(): User {
        return User(
            userPreference.id,
            userPreference.firstName,
            userPreference.surName,
            userPreference.patronymic,
            userPreference.groupId.let {
                if (userPreference.groupId.isNotEmpty())
                    it
                else
                    null
            },
            User.Role.valueOf(userPreference.role),
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
        requireAllowWriteData()
        val userDoc = userMapper.domainToDoc(user)
        usersRef.document(user.id).set(userDoc)
            .await()
    }

    suspend fun update(user: UserEntity) {
        requireAllowWriteData()
        userDao.update(user)
        val userDoc = userMapper.entityToDoc(user)
        usersRef.document(user.id).set(userDoc)
            .await()

    }

    open suspend fun remove(user: User) {
        requireAllowWriteData()
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

    suspend fun findAndSaveByPhoneNum(phoneNum: String) {
        requireAllowWriteData()
        usersRef.whereEqualTo("phoneNum", phoneNum)
            .get()
            .await().apply {
                if (isEmpty) {
                    throw Exception("Не верный номер")
                }
                val user = documents[0].toObject(
                    User::class.java
                )
                saveUserPreference(user!!)
            }
    }

    suspend fun findAndSaveByEmail(email: String) {
        Log.d("lol", "AA usersRef.whereEqualTo: ")
        usersRef.whereEqualTo("email", email)
            .get()
            .await().apply {
                Log.d("lol", "A whereEqualTo awaited: ")
                if (isEmpty) {
                    throw  FirebaseAuthException(
                        "ERROR_USER_NOT_FOUND",
                        "Nothing user!"
                    )
                }
                val user = documents[0].toObject(User::class.java)!!
                Log.d("lol", "A saveUserPreference: ")
                saveUserPreference(user)
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
                        saveUserPreference(user)
                        trySend(user)
                    }
                }
            }
        awaitClose { }
    }

    fun observeById(userId: String): Flow<User?> = flow {
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
        emitAll(
            userDao.observe(userId)
                .map { entity: UserEntity? -> entity?.let { userMapper.entityToDomain(entity) } }
        )
    }
}