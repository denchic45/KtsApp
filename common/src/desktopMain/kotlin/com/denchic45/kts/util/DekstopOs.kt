package com.denchic45.kts.util

import java.util.*


enum class OS {
    WINDOWS, LINUX, MAC
}

val currentOs: OS
    get() {
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)
        return when {
            os.contains("win") -> {
                OS.WINDOWS
            }
            os.contains("nix") || os.contains("nux") || os.contains("aix") -> {
                OS.LINUX
            }
            os.contains("mac") -> {
                OS.MAC
            }
            else -> throw IllegalStateException("Unknown OS:$os")
        }
    }