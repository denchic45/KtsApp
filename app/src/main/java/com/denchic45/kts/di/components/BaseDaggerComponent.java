package com.denchic45.kts.di.components;

public interface BaseDaggerComponent {

    interface Factory<T> {

        T create();
    }

    interface Builder<T> {
        T build();
    }
}
