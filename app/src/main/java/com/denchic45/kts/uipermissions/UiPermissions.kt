package com.denchic45.kts.uipermissions

import com.denchic45.kts.data.model.domain.User

class UiPermissions(private val user: User) {
    private val permissions: MutableSet<Permission> = mutableSetOf()

    fun putPermissions(vararg permission: Permission): Boolean {
        return permissions.addAll(mutableListOf(*permission))
    }

    fun runIfHasPermission(permissionName: String, action: Runnable) {
        if (isAllowed(permissionName)) {
            action.run()
        }
    }

    fun runIfHasPermissionOrElse(permissionName: String, action: Runnable, actionElse: Runnable) {
        if (isAllowed(permissionName)) {
            action.run()
        } else {
            actionElse.run()
        }
    }

    fun isAllowed(permissionName: String): Boolean {
        return getPermission(permissionName).isAllowedRole(user)
    }

    fun isNotAllowed(permissionName: String): Boolean {
        return isAllowed(permissionName)
    }

    private fun getPermission(permissionName: String): Permission {
        return permissions.stream()
            .filter { permission: Permission -> permission.name == permissionName }
            .findFirst()
            .get()
    }
}