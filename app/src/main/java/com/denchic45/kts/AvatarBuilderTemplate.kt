package com.denchic45.kts;

import androidx.core.content.res.ResourcesCompat;

import com.denchic45.avatarGenerator.AvatarGenerator;
import com.denchic45.avatarGenerator.AvatarBuilderInitializer;

import org.jetbrains.annotations.NotNull;

public class AvatarBuilderTemplate implements AvatarBuilderInitializer {
    @Override
    public AvatarGenerator.Builder initBuilder(@NotNull AvatarGenerator.Builder builder) {
        return builder
                .font(ResourcesCompat.getFont(builder.getContext(), R.font.futura_medium));
    }
}
