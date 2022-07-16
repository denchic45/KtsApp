package com.denchic45.kts.data.pref.core

interface PlatformPreferences {

    interface Factory {
        fun create(name: String? = null): PlatformPreferences
    }

    fun clear()

    fun remove(key: String)

    fun hasKey(key: String): Boolean

    fun putInt(key: String, value: Int)

    fun getInt(key: String, defaultValue: Int): Int

    fun getIntOrNull(key: String): Int?

    fun putLong(key: String, value: Long)

    fun getLong(key: String, defaultValue: Long): Long

    fun getLongOrNull(key: String): Long?

    fun putString(key: String, value: String)

    fun getString(key: String, defaultValue: String): String

    fun getStringOrNull(key: String): String?

    fun putFloat(key: String, value: Float)

    fun getFloat(key: String, defaultValue: Float): Float

    fun getFloatOrNull(key: String): Float?

    fun putDouble(key: String, value: Double)

    fun getDouble(key: String, defaultValue: Double): Double

    fun getDoubleOrNull(key: String): Double?

    fun putBoolean(key: String, value: Boolean)

    fun getBoolean(key: String, defaultValue: Boolean): Boolean

    fun getBooleanOrNull(key: String): Boolean?
}