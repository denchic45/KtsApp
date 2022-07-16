package com.denchic45.kts.util

import net.harawata.appdirs.AppDirsFactory

@Deprecated("")
val appDir: String
    get() {
        return when (currentOs) {
            OS.WINDOWS -> System.getenv("APPDATA") + "\\KtsApp"
            OS.MAC -> TODO()
            OS.LINUX -> TODO()
        }
    }

val appDirectory: String
    get() = AppDirsFactory
        .getInstance()
        .getUserDataDir("KtsApp", null, null, true)
