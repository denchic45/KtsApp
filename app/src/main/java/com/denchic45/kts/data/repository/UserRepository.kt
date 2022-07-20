package com.denchic45.kts.data.repository

import com.denchic45.kts.data.local.db.UserLocalDataSource
import com.denchic45.kts.data.mapper.*
import com.denchic45.kts.data.pref.UserPreferences
import com.denchic45.kts.data.remote.db.UserRemoteDataSource
import com.denchic45.kts.data.remote.storage.UserRemoteStorage
import com.denchic45.kts.data.service.AppVersionService
import com.denchic45.kts.data.service.NetworkService
import com.denchic45.kts.domain.model.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

open class UserRepository @Inject constructor(
    override val appVersionService: AppVersionService,
    private val userPreferences: UserPreferences,
    override val networkService: NetworkService,
    private val userLocalDataSource: UserLocalDataSource,
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userRemoteStorage: UserRemoteStorage,
) : Repository(), FindByContainsNameRepository<User> {

    override fun findByContainsName(text: String): Flow<List<User>> {
        return userRemoteDataSource.findByContainsName(text)
            .map { users ->
                userLocalDataSource.upsert(users.mapsToUserEntities())
                users.mapsToUsers()
            }
    }

    private fun saveUserPreference(user: User) {
        userPreferences.id = user.id
        userPreferences.firstName = user.firstName
        userPreferences.patronymic = user.patronymic ?: ""
        userPreferences.surname = user.surname
        userPreferences.role = user.role.toString()
        userPreferences.gender = user.gender
        userPreferences.photoUrl = user.photoUrl
        userPreferences.email = user.email ?: ""
        userPreferences.isAdmin = user.admin
        userPreferences.timestamp = user.timestamp!!.time
        userPreferences.isGeneratedAvatar = user.generatedAvatar
        user.groupId?.let { userPreferences.groupId = it }
    }

    fun findSelf(): User {
        return User(
            userPreferences.id,
            userPreferences.firstName,
            userPreferences.surname,
            userPreferences.patronymic,
            userPreferences.groupId.let {
                if (userPreferences.groupId.isNotEmpty())
                    it
                else
                    null
            },
            User.Role.valueOf(userPreferences.role),
            userPreferences.email,
            userPreferences.photoUrl,
            Date(userPreferences.timestamp), userPreferences.gender,
            userPreferences.isGeneratedAvatar, userPreferences.isAdmin
        )
    }


    suspend fun loadAvatar(avatarBytes: ByteArray, userId: String): String {
        return userRemoteStorage.uploadAvatar(avatarBytes, userId)
    }

    suspend fun findAndSaveByEmail(email: String) {
        saveUserPreference(
            userRemoteDataSource.findAndByEmail(email).mapToUser()
        )
    }

    val thisUserObserver: Flow<User?> = callbackFlow {
        userRemoteDataSource.observeById(userPreferences.id)
            .catch { close(it) }
            .collect { value ->
                if (value == null) {
                    trySend(null)
                    Unit
                } else {
                    val user = value.mapToUser()
                    if (user.timestamp != null) {
                        saveUserPreference(user)
                        trySend(user)
                    }
                }
            }

        awaitClose { }
    }

    fun observeById(userId: String): Flow<User?> = flow {
        if (!userLocalDataSource.isExist(userId)) {
            userRemoteDataSource.observeById(userId)
                .collect { map ->
                    map?.let {
                        userLocalDataSource.upsert(
                            map.mapToUserEntity()
                        )
                    }
                }
        }
        emitAll(
            userLocalDataSource.observe(userId)
                .distinctUntilChanged()
                .map { it?.toDomain() }
        )
    }
}