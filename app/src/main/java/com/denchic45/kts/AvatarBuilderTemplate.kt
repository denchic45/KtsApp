package com.denchic45.kts

import androidx.core.content.res.ResourcesCompat
import com.denchic45.avatarGenerator.AvatarBuilderInitializer
import com.denchic45.avatarGenerator.AvatarGenerator

class AvatarBuilderTemplate : AvatarBuilderInitializer {
    override fun initBuilder(builder: AvatarGenerator.Builder): AvatarGenerator.Builder {
        return builder
            .font(ResourcesCompat.getFont(builder.context, R.font.futura_medium))
    }
}