package com.denchic45.kts.data.prefs

import android.content.Context
import javax.inject.Inject

class UserPreference @Inject constructor(context: Context) : BaseSharedPreference(context, "User") {
    var firstName: String
        get() = getValue(FIRST_NAME, "No")
        set(firstName) = setValue(FIRST_NAME, firstName)

    var surName: String
        set(value) = setValue(SURNAME, value)
        get() = getValue(SURNAME, "Name")

    var gender: Int
        get() = getValue(GENDER, 0)
        set(gender) = setValue(GENDER, gender)

    var role: String
        get() = getValue(ROLE, "")
        set(role) = setValue(ROLE, role)

    var photoUrl: String
        get() = getValue(PHOTO_URL, "")
        set(role) = setValue(PHOTO_URL, role)

    var id: String
        get() = getValue(ID, "")
        set(id) = setValue(ID, id)

    var isAdmin: Boolean
        get() = getValue(ADMIN, false)
        set(admin) = setValue(ADMIN, admin)

    var isGeneratedAvatar: Boolean
        get() = getValue(GENERATED_AVATAR, true)
        set(generatedAvatar) = setValue(GENERATED_AVATAR, generatedAvatar)

    var timestamp: Long
        get() = getValue(TIMESTAMP, 0L)
        set(timestamp) = setValue(TIMESTAMP, timestamp)

    var email: String
        get() = getValue(EMAIL, "")
        set(email) = setValue(EMAIL, email)

    var patronymic: String
        get() = getValue(PATRONYMIC, "")
        set(email) = setValue(PATRONYMIC, email)

    var groupId: String
        get() = getValue(GROUP_ID, "")
        set(groupId) = setValue(GROUP_ID, groupId)

    companion object {
        const val FIRST_NAME = "FIRST_NAME"
        const val SURNAME = "SURNAME"
        const val PATRONYMIC = "PATRONYMIC"
        const val ROLE = "ROLE"
        const val PHOTO_URL = "PHOTO_URL"
        const val PHONE_NUM = "PHONE_NUM"
        const val EMAIL = "EMAIL"
        const val GENDER = "GENDER"
        const val ID = "ID"
        const val ADMIN = "ADMIN"
        const val GROUP_ID = "GROUP_ID"
        private const val TIMESTAMP = "TIMESTAMP"
        private const val GENERATED_AVATAR = "GENERATED_AVATAR"
    }
}