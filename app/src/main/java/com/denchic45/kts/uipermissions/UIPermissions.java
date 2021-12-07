package com.denchic45.kts.uipermissions;

import com.denchic45.kts.data.model.domain.User;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class UIPermissions {

    private final User user;
    private final List<Permission> permissions;

    public UIPermissions(User user) {
        this.user = user;
        permissions = new ArrayList<>();
    }


    public boolean addPermissions(Permission... permission) {
        return permissions.addAll(Arrays.asList(permission));
    }

    public void runIfHasPermission(String permissionName, Runnable action) {
        if (isAllowed(permissionName)) {
            action.run();
        }
    }

    public void runIfHasPermissionOrElse(String permissionName, Runnable action, Runnable actionElse) {
        if (isAllowed(permissionName)) {
            action.run();
        } else {
            actionElse.run();
        }
    }

    public boolean isAllowed(String permissionName) {
        return getPermission(permissionName).isAllowedRole(user);
    }

    public boolean isNotAllowed(String permissionName) {
        return isAllowed(permissionName);
    }

    @NotNull
    private Permission getPermission(String permissionName) {
        return Objects.requireNonNull(permissions.stream()
                .filter(permission -> permission.getName().equals(permissionName))
                .findFirst()
                .orElse(null));
    }
}
