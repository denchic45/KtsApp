package com.denchic45.kts.data.prefs.core

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.denchic45.kts.data.pref.core.PlatformPreferences

class AndroidPreferences(val delegate: SharedPreferences) : PlatformPreferences {

    class Factory(context: Context) : PlatformPreferences.Factory {
        private val appContext = context.applicationContext

        override fun create(name: String?): PlatformPreferences {
            // For null name, match the behavior of PreferenceManager.getDefaultSharedPreferences()
            val preferencesName = name ?: "${appContext.packageName}_preferences"
            val delegate = appContext.getSharedPreferences(preferencesName, MODE_PRIVATE)
            return AndroidPreferences(delegate)
        }
    }

    override fun clear() {
        // Note: we call remove() on all keys instead of calling clear() in order to match listener behavior to iOS
        // See issue #9
        delegate.edit().apply {
            for (key in delegate.all.keys) {
                remove(key)
            }
        }.apply()
    }

    override fun remove(key: String): Unit = delegate.edit().remove(key).apply()

    override fun hasKey(key: String): Boolean = delegate.contains(key)

    override fun putInt(key: String, value: Int): Unit =
        delegate.edit().putInt(key, value).apply()

    override fun getInt(key: String, defaultValue: Int): Int =
        delegate.getInt(key, defaultValue)

    override fun getIntOrNull(key: String): Int? =
        if (delegate.contains(key)) delegate.getInt(key, 0) else null

    override fun putLong(key: String, value: Long): Unit =
        delegate.edit().putLong(key, value).apply()

    override fun getLong(key: String, defaultValue: Long): Long =
        delegate.getLong(key, defaultValue)

    override fun getLongOrNull(key: String): Long? =
        if (delegate.contains(key)) delegate.getLong(key, 0L) else null

    override fun putString(key: String, value: String): Unit =
        delegate.edit().putString(key, value).apply()

    override fun getString(key: String, defaultValue: String): String =
        delegate.getString(key, defaultValue) ?: defaultValue

    override fun getStringOrNull(key: String): String? =
        if (delegate.contains(key)) delegate.getString(key, "") else null

    override fun putFloat(key: String, value: Float): Unit =
        delegate.edit().putFloat(key, value).apply()

    override fun getFloat(key: String, defaultValue: Float): Float =
        delegate.getFloat(key, defaultValue)

    override fun getFloatOrNull(key: String): Float? =
        if (delegate.contains(key)) delegate.getFloat(key, 0f) else null

    override fun putDouble(key: String, value: Double): Unit =
        delegate.edit().putLong(key, value.toRawBits()).apply()

    override fun getDouble(key: String, defaultValue: Double): Double =
        Double.fromBits(delegate.getLong(key, defaultValue.toRawBits()))

    override fun getDoubleOrNull(key: String): Double? =
        if (delegate.contains(key)) Double.fromBits(
            delegate.getLong(
                key,
                0.0.toRawBits()
            )
        ) else null


    override fun putBoolean(key: String, value: Boolean): Unit =
        delegate.edit().putBoolean(key, value).apply()

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        delegate.getBoolean(key, defaultValue)

    override fun getBooleanOrNull(key: String): Boolean? =
        if (delegate.contains(key)) delegate.getBoolean(key, false) else null
}