package com.denchic45.kts.uipermissions;

import com.denchic45.kts.data.model.domain.User;

import java.util.Arrays;
import java.util.function.Predicate;

public class Permission {

    private final Predicate<User>[] userPredicate;
    private final String name;

    @SafeVarargs
    public Permission(String name, Predicate<User>... predicate) {
        this.name = name;
        this.userPredicate = predicate;
    }

    public boolean isAllowedRole(User user) {
        return Arrays.stream(userPredicate).anyMatch(userPredicate1 -> userPredicate1.test(user));
    }

    public String getName() {
        return name;
    }
}
