package com.denchic45.kts.data.pref

import com.russhwolf.settings.Settings

class UserPreferences(val settings: Settings) : Settings by settings {

    companion object {
        const val FIRST_NAME = "FIRST_NAME"
        const val SURNAME = "SURNAME"
        const val PATRONYMIC = "PATRONYMIC"
        const val ROLE = "ROLE"
        const val PHOTO_URL = "PHOTO_URL"
        const val EMAIL = "EMAIL"
        const val GENDER = "GENDER"
        const val ID = "ID"
        const val ADMIN = "ADMIN"
        const val GROUP_ID = "GROUP_ID"
        private const val TIMESTAMP = "TIMESTAMP"
        private const val GENERATED_AVATAR = "GENERATED_AVATAR"
    }

    var id: String
        set(id) = putString(ID, id)
        get() = getString(ID, "")

    var firstName: String
        set(value) = putString(FIRST_NAME, value)
        get() = getString(FIRST_NAME, "")

    var surname: String
        set(value) = putString(SURNAME, value)
        get() = getString(SURNAME, "")

    var patronymic: String
        set(email) = putString(PATRONYMIC, email)
        get() = getString(PATRONYMIC, "")

    var gender: Int
        set(gender) = putInt(GENDER, gender)
        get() = getInt(GENDER, 0)

    var role: String
        set(role) = putString(ROLE, role)
        get() = getString(ROLE, "")

    var groupId: String
        set(groupId) = putString(GROUP_ID, groupId)
        get() = getString(GROUP_ID, "")

    var photoUrl: String
        set(role) = putString(PHOTO_URL, role)
        get() = getString(PHOTO_URL, "")

    var timestamp: Long
        set(timestamp) = putLong(TIMESTAMP, timestamp)
        get() = getLong(TIMESTAMP, 0L)

    var email: String
        set(email) = putString(EMAIL, email)
        get() = getString(EMAIL, "")

    var isGeneratedAvatar: Boolean
        set(generatedAvatar) = putBoolean(GENERATED_AVATAR, generatedAvatar)
        get() = getBoolean(GENERATED_AVATAR, true)

    var isAdmin: Boolean
        set(admin) = putBoolean(ADMIN, admin)
        get() = getBoolean(ADMIN, false)

}