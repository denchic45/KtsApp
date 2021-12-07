package com.denchic45.kts.data.model.domain

import androidx.annotation.StringDef
import com.denchic45.kts.data.model.DomainModel
import java.util.*

data class User(
    override var uuid: String = UUID.randomUUID().toString(),
    val firstName: String,
    val surname: String,
    val patronymic: String?,
    val groupUuid: String?,
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
        return groupUuid != null && groupUuid != ""
    }

    val fullName: String
        get() = "$firstName $surname"
    val isCurator: Boolean
        get() = isTeacher && hasGroup()

    fun isIt(user: User): Boolean {
        return user.uuid == uuid
    }

    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    @StringDef(STUDENT, DEPUTY_MONITOR, CLASS_MONITOR, TEACHER, HEAD_TEACHER)
    annotation class Role

    override fun copy(): User {
        return User(
            uuid,
            firstName,
            surname,
            patronymic,
            groupUuid,
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