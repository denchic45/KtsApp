package com.denchic45.kts.ui.profile.fullAvatar

import android.content.Context
import com.denchic45.avatarGenerator.AvatarGenerator
import com.denchic45.kts.AvatarBuilderTemplate
import com.denchic45.kts.data.Interactor
import com.denchic45.kts.data.model.domain.User
import com.denchic45.kts.data.repository.StudentRepository
import com.denchic45.kts.data.repository.TeacherRepository
import com.denchic45.kts.data.repository.UserRepository
import javax.inject.Inject

class FullAvatarInteractor @Inject constructor(
    context: Context,
    private val userRepository: UserRepository,
    private val studentRepository: StudentRepository,
    private val teacherRepository: TeacherRepository,
) : Interactor() {

    private val avatarGenerator: AvatarGenerator.Builder = AvatarGenerator.Builder(context)

    fun findThisUser(): User {
        return userRepository.findThisUser()
    }

    suspend fun removeUserAvatar(user: User) {
        val photoUrl = userRepository.loadAvatar(
            avatarGenerator.name(user.firstName)
                .initFrom(AvatarBuilderTemplate())
                .generateBytes(), user.uuid
        )
        val updatedUser = user.copy(photoUrl = photoUrl, generatedAvatar = true)
        when {
            updatedUser.isStudent -> studentRepository.update(updatedUser)
            updatedUser.isTeacher -> teacherRepository.update(updatedUser)
            else -> throw IllegalStateException()
        }
    }

    override fun removeListeners() {}

}