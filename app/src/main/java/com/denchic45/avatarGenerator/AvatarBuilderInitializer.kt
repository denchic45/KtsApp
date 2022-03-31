package com.denchic45.avatarGenerator

interface AvatarBuilderInitializer {
    fun initBuilder(builder: AvatarGenerator.Builder): AvatarGenerator.Builder
}