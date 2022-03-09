package com.denchic45.kts.ui.userEditor

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.denchic45.avatarGenerator.AvatarGenerator
import com.denchic45.kts.AvatarBuilderTemplate
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.model.domain.User.Companion.isStudent
import com.denchic45.kts.data.model.domain.User.Companion.isTeacher
import com.denchic45.kts.data.repository.*
import com.denchic45.kts.utils.NetworkException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserEditorInteractor @Inject constructor(
    context: Context,
    private val groupRepository: GroupRepository,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
    private val authRepository: AuthRepository,
    val networkService: NetworkService
) : Interactor {

    private val avatarGenerator: AvatarGenerator.Builder = AvatarGenerator.Builder(context)

    fun getGroupsByTypedName(name: String): Flow<List<CourseGroup>> {
        return groupRepository.findByTypedName(name)
    }

    fun getGroupNameById(groupId: String): LiveData<String> {
        return groupRepository.getNameByGroupId(groupId).asLiveData()
    }

    private suspend fun loadAvatar(avatarBytes: ByteArray, id: String): String {
        return userRepository.loadAvatar(avatarBytes, id)
    }

    fun addUser(user: User): Flow<Resource<User>> {
        return flow {
            if (!networkService.isNetworkAvailable) {
                emit(Resource.Error(NetworkException()))
            }
            val photoUrl = createAvatar(user)
            val updatedUser = user.copy(photoUrl = photoUrl)
            emit(Resource.Next(updatedUser, "LOAD_AVATAR"))
            when {
                isStudent(updatedUser.role) -> studentRepository.add(updatedUser)
                isTeacher(updatedUser.role) -> teacherRepository.add(updatedUser)
            }
            emit(Resource.Success(updatedUser))
            return@flow
        }
    }

    fun updateUser(user: User): Flow<Resource<User>> = flow {
        if (!networkService.isNetworkAvailable) {
            emit(Resource.Error(NetworkException()))
        } else {
            val photoUrl = createAvatar(user)
            val updatedUser = user.copy(photoUrl = photoUrl)
            emit(Resource.Next(updatedUser, "LOAD_AVATAR"))
            when {
                isStudent(updatedUser.role) -> studentRepository.update(updatedUser)
                isTeacher(updatedUser.role) -> teacherRepository.update(updatedUser)
                else -> throw IllegalStateException()
            }
            emit(Resource.Success(updatedUser))
            return@flow
        }
    }

    private suspend fun createAvatar(user: User): String {
        val photoUrl: String = if (user.generatedAvatar) {
            val avatarBytes = avatarGenerator.name(user.firstName)
                .initFrom(AvatarBuilderTemplate())
                .generateBytes()
            loadAvatar(avatarBytes, user.id)
        } else {
            return user.photoUrl
        }
        return photoUrl
    }

    suspend fun removeStudent(student: User) {
        return studentRepository.remove(student)
    }

    suspend fun removeTeacher(teacher: User) {
        return teacherRepository.remove(teacher)
    }

    fun observeUserById(userId: String): Flow<User?> {
        return userRepository.observeById(userId)
    }

    override fun removeListeners() {
        userRepository.removeListeners()
        groupRepository.removeListeners()
    }

    fun signUpUser(email: String, password: String) {
        authRepository.signUpNewUser(email, password)
    }

}