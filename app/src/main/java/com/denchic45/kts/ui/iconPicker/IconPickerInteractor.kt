package com.denchic45.kts.ui.iconPicker

import kotlinx.coroutines.channels.Channel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IconPickerInteractor @Inject constructor() {
    private var selectedIcon = Channel<String>()

    suspend fun postSelectedIcon(icon: String) {
        selectedIcon.send(icon)
    }

    suspend fun observeSelectedIcon(): String {
        return selectedIcon.receive()
    }
}