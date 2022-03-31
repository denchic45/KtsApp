package com.denchic45.kts.uipermissions

import com.denchic45.kts.data.model.domain.User

class UiPermissions(private val user: User) {
    private val permissions: MutableMap<String, Permission> = mutableMapOf()

    fun putPermissions(vararg permission: Permission) {
        return permissions.putAll(permission.associateBy { it.name })
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
        return !getPermission(permissionName).isAllowedRole(user)
    }

    private fun getPermission(permissionName: String): Permission {
        return permissions.getValue(permissionName)
    }
}