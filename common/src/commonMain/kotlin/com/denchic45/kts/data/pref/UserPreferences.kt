package com.denchic45.kts.data.pref

import com.russhwolf.settings.*

class UserPreferences(val settings: Settings) : Settings by settings {
    var id: String by string()
    var firstName: String by string()
    var surname: String by string()
    var patronymic: String by string()
    var gender: Int by int()
    var role: String by string()
    var groupId: String by string()
    var photoUrl: String by string()
    var timestamp: Long by long()
    var email: String by string()
    var isGeneratedAvatar: Boolean by boolean(defaultValue = true)
    var isAdmin: Boolean by boolean(defaultValue = true)

}