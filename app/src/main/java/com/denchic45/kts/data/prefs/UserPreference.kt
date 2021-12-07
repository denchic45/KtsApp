package com.denchic45.kts.data.prefs

import android.content.Context
import javax.inject.Inject

class UserPreference @Inject constructor(context: Context) : BaseSharedPreference(context, "User") {
    var firstName: String
        get() = getValue(FIRST_NAME, "No")
        set(firstName) {
            setValue(FIRST_NAME, firstName)
        }
    val surName: String
        get() = getValue(SURNAME, "Name")

    fun setSurname(surname: String) {
        setValue(SURNAME, surname)
    }

    var gender: Int
        get() = getValue(GENDER, 0)
        set(gender) {
            setValue(GENDER, gender)
        }
    var role: String
        get() = getValue(ROLE, "")
        set(role) {
            setValue(ROLE, role)
        }
    var photoUrl: String
        get() = getValue(PHOTO_URL, "")
        set(role) {
            setValue(PHOTO_URL, role)
        }
    var phoneNum: String
        get() = getValue(PHONE_NUM, "")
        set(phoneNum) {
            setValue(PHONE_NUM, phoneNum)
        }
    var uuid: String
        get() = getValue(UUID, "")
        set(uuid) {
            setValue(UUID, uuid)
        }
    var isAdmin: Boolean
        get() = getValue(ADMIN, false)
        set(admin) {
            setValue(ADMIN, admin)
        }
    var isGeneratedAvatar: Boolean
        get() = getValue(GENERATED_AVATAR, true)
        set(generatedAvatar) {
            setValue(GENERATED_AVATAR, generatedAvatar)
        }
    var timestamp: Long
        get() = getValue(TIMESTAMP, 0L)
        set(timestamp) {
            setValue(TIMESTAMP, timestamp)
        }
    var email: String
        get() = getValue(EMAIL, "")
        set(email) {
            setValue(EMAIL, email)
        }
    var patronymic: String
        get() = getValue(PATRONYMIC, "")
        set(email) {
            setValue(PATRONYMIC, email)
        }

    companion object {
        const val FIRST_NAME = "FIRST_NAME"
        const val SURNAME = "SURNAME"
        const val PATRONYMIC = "PATRONYMIC"
        const val ROLE = "ROLE"
        const val PHOTO_URL = "PHOTO_URL"
        const val PHONE_NUM = "PHONE_NUM"
        const val EMAIL = "EMAIL"
        const val GENDER = "GENDER"
        const val UUID = "UUID"
        const val ADMIN = "ADMIN"
        private const val TIMESTAMP = "TIMESTAMP"
        private const val GENERATED_AVATAR = "GENERATED_AVATAR"
    }
}