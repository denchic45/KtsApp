package com.denchic45.kts

import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.util.Log
import androidx.lifecycle.LiveData

abstract class SharedPreferenceLiveData<T>(
    val sharedPrefs: SharedPreferences,
    val key: String,
    var defValue: T
) : LiveData<T>() {
    private val preferenceChangeListener =
        OnSharedPreferenceChangeListener { sharedPreferences, key ->
            Log.d("lol", "onSharedPreferenceChanged: $key")
            if (this@SharedPreferenceLiveData.key == key) {
                value = getValue(key, defValue)
            }
        }

    abstract fun getValue(key: String, defValue: T): T
    override fun onActive() {
        super.onActive()
        value = getValue(key, defValue)
        sharedPrefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        sharedPrefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }

    fun getLiveData(key: String, defaultValue: Boolean): SharedPreferenceLiveData<Boolean> {
        return SharedPreferenceBooleanLiveData(sharedPrefs, key, defaultValue)
    }

    fun getLiveData(key: String, defaultValue: String): SharedPreferenceLiveData<String> {
        return SharedPreferenceStringLiveData(sharedPrefs, key, defaultValue)
    }

    class SharedPreferenceBooleanLiveData(
        prefs: SharedPreferences,
        key: String,
        defValue: Boolean
    ) : SharedPreferenceLiveData<Boolean>(prefs, key, defValue) {
        override fun getValue(key: String, defValue: Boolean): Boolean {
            return sharedPrefs.getBoolean(key, defValue)
        }
    }

    class SharedPreferenceStringLiveData(prefs: SharedPreferences, key: String, defValue: String) :
        SharedPreferenceLiveData<String>(prefs, key, defValue) {
        override fun getValue(key: String, defValue: String): String {
            return sharedPrefs.getString(key, defValue)!!
        }
    }

    class SharedPreferenceIntegerLiveData(prefs: SharedPreferences, key: String, defValue: Int) :
        SharedPreferenceLiveData<Int>(prefs, key, defValue) {
        override fun getValue(key: String, defValue: Int): Int {
            return sharedPrefs.getInt(key, defValue)
        }
    }

    class SharedPreferenceLongLiveData(prefs: SharedPreferences, key: String, defValue: Long) :
        SharedPreferenceLiveData<Long>(prefs, key, defValue) {
        override fun getValue(key: String, defValue: Long): Long {
            return sharedPrefs.getLong(key, defValue)
        }
    }
}