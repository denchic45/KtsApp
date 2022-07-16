package com.denchic45.kts.data.pref.core

import com.russhwolf.settings.JvmPreferencesSettings

class Main {
    init {
        JvmPreferencesSettings(FilePreferencesFactory("").userRoot())
    }
}