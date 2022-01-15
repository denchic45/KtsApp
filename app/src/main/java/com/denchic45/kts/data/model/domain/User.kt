package com.denchic45.kts.data.model.domain

import androidx.annotation.StringDef
import com.denchic45.kts.data.model.DomainModel
import java.util.*

data class User(
    override var id: String = UUID.randomUUID().toString(),
    val firstName: String,
    val surname: String,
    val patronymic: String?,
    val groupId: String?,
    val role: String,
    val phoneNum: String,
    val email: String?,
    val photoUrl: String,
    val timestamp: Date?,
    val gender: Int,
    val generatedAvatar: Boolean,
    val admin: Boolean
) : DomainModel() {

  private constructor(): this(
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      "",
      null,
      0,
      false,
      false
        )


    val isTeacher: Boolean
        get() = role == TEACHER || role == HEAD_TEACHER
    val isStudent: Boolean
        get() = role == STUDENT || role == DEPUTY_MONITOR || role == CLASS_MONITOR


    fun hasGroup(): Boolean {
        return groupId != null && groupId != ""
    }

    val fullName: String
        get() = "$firstName $surname"
    val isCurator: Boolean
        get() = isTeacher && hasGroup()

    fun isIt(user: User): Boolean {
        return user.id == id
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(STUDENT, DEPUTY_MONITOR, CLASS_MONITOR, TEACHER, HEAD_TEACHER)
    annotation class Role

    override fun copy(): User {
        return User(
            id,
            firstName,
            surname,
            patronymic,
            groupId,
            role,
            phoneNum,
            email,
            photoUrl,
            timestamp,
            gender,
            generatedAvatar,
            admin
        )
    }

    companion object {
        const val STUDENT = "STUDENT"
        const val DEPUTY_MONITOR = "DEPUTY_MONITOR"
        const val CLASS_MONITOR = "CLASS_MONITOR"
        const val TEACHER = "TEACHER"
        const val HEAD_TEACHER = "HEAD_TEACHER"

        @JvmStatic
        fun isTeacher(role: String): Boolean {
            return role == TEACHER || role == HEAD_TEACHER
        }

        @JvmStatic
        fun isStudent(role: String): Boolean {
            return role == STUDENT || role == CLASS_MONITOR || role == DEPUTY_MONITOR
        }

        @JvmStatic
        fun createEmpty(): User {
            return User()
        }
    }
}