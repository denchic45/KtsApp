package com.denchic45.kts.data.remote.model

import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.domain.model.User
import com.denchic45.kts.util.SearchKeysGenerator
import java.util.*

data class UserDoc(
    var id: String = UUID.randomUUID().toString(),
    var firstName: String,
    var surname: String,
    var patronymic: String? = null,
    var groupId: String? = null,
    var role: User.Role,
    var email: String? = null,
    var photoUrl: String,
    //    @ServerTimestamp
    val timestamp: Date? = null,
    var gender: Int,
    var generatedAvatar: Boolean,
    var admin: Boolean,

    ) : DocModel {

    val searchKeys: List<String>
        get() = SearchKeysGenerator().generateKeys(fullName) { predicate: String -> predicate.length > 2 }

    private constructor() : this(
        "",
        "",
        "",
        "",
        "",
        User.Role.STUDENT,
        "",
        "",
        Date(),
        0,
        true,
        false
    )

    companion object {
        fun createEmpty() = UserDoc()
    }

    val isTeacher: Boolean
        get() {
            return role == User.Role.TEACHER || role == User.Role.HEAD_TEACHER
        }
    val isStudent: Boolean
        get() = role == User.Role.STUDENT
    val fullName: String
        get() = "$firstName $surname"
}