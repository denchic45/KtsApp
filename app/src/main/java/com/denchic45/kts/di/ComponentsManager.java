package com.denchic45.kts.di;

import com.denchic45.kts.di.components.BaseDaggerComponent;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ComponentsManager {
    public static Map<String, BaseDaggerComponent> components = new HashMap<>();

    public static <T extends BaseDaggerComponent> T getComponent(@NotNull Class<T> componentClass) {
        String name = componentClass.getSimpleName();
        BaseDaggerComponent component = components.get(name);
        if (component != null)
            return componentClass.cast(component);
        throw new RuntimeException("Nothing by: " + name);
    }

    public static <T extends BaseDaggerComponent> @NotNull T plusComponent(T component) {
        components.put(component.getClass().getSimpleName().replace("Impl",""), component);
        return component;
    }

    public static void destroyComponent(@NotNull Class<? extends BaseDaggerComponent> component) {
        components.remove(component.getSimpleName());
    }
}
