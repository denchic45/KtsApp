package com.denchic45.kts.uipermissions

import com.denchic45.kts.domain.model.User

class Permission @SafeVarargs constructor(val name: String, vararg predicate:  User.() -> Boolean) {
    private val userPredicate: Array<out (User) -> Boolean> = predicate
    fun isAllowedRole(user: User): Boolean {
        return userPredicate.any { it(user) }
    }

}