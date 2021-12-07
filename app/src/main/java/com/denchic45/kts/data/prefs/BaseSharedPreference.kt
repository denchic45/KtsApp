package com.denchic45.kts.data.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.denchic45.kts.SharedPreferenceLiveData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

open class BaseSharedPreference @SuppressLint("CommitPrefEdits") constructor(
    context: Context,
    prefsName: String
) {


    private val prefs: SharedPreferences =
        context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)

    val callback: KeyFlow = prefs.keyFlow.onStart { emit("") }

    private val edit: SharedPreferences.Editor = prefs.edit()
    protected fun getValue(key: String, defValue: String): String {
        return prefs.getString(key, defValue)!!
    }

    protected fun setValue(key: String, value: String?) {
        edit.putString(key, value).apply()
    }

    protected fun getValue(key: String, defValue: Int): Int {
        return prefs.getInt(key, defValue)
    }

    protected fun setValue(key: String, value: Int) {
        edit.putInt(key, value).apply()
    }

    protected fun getValue(key: String, defValue: Boolean): Boolean {
        return prefs.getBoolean(key, defValue)
    }

    protected fun setValue(key: String, value: Boolean) {
        edit.putBoolean(key, value).apply()
    }

    protected fun getValue(key: String, defValue: Long): Long {
        return prefs.getLong(key, defValue)
    }

    protected fun setValue(key: String, value: Long) {
        edit.putLong(key, value).apply()
    }

    fun observeValueLiveData(key: String, value: String): SharedPreferenceLiveData<String> {
        return SharedPreferenceLiveData.SharedPreferenceStringLiveData(prefs, key, value)
    }

    fun observeValueLiveData(key: String, value: Boolean): SharedPreferenceLiveData<Boolean> {
        return SharedPreferenceLiveData.SharedPreferenceBooleanLiveData(prefs, key, value)
    }

    fun observeValueLiveData(key: String, value: Int): SharedPreferenceLiveData<Int> {
        return SharedPreferenceLiveData.SharedPreferenceIntegerLiveData(prefs, key, value)
    }

    fun observeValueLiveData(key: String, value: Long): SharedPreferenceLiveData<Long> {
        return SharedPreferenceLiveData.SharedPreferenceLongLiveData(prefs, key, value)
    }

    @ExperimentalCoroutinesApi
    fun observeValue(key: String, value: String): Flow<String> {
        return prefs.keyFlow.filter { it == key }
            .onStart { emit(key) }
            .map { getValue(key, value) }
    }

    @ExperimentalCoroutinesApi
    fun observeValue(key: String, value: Boolean): Flow<Boolean> {
        return prefs.keyFlow.filter { it == key }
            .onStart { emit(key) }
            .map { getValue(key, value) }
    }

    @ExperimentalCoroutinesApi
    fun observeValue(key: String, value: Int): Flow<Int> {
        return prefs.keyFlow.filter { it == key }
            .onStart { emit(key) }
            .map { getValue(key, value) }
    }

    @ExperimentalCoroutinesApi
    fun observeValue(key: String, value: Long): Flow<Long> {
        return prefs.keyFlow.filter { it == key }
            .onStart { emit(key) }
            .map { getValue(key, value) }
    }

//    protected fun observeValue(
//        key: String,
//        value: Boolean
//    ): SharedPreferenceLiveData.SharedPreferenceBooleanLiveData {
//        return SharedPreferenceLiveData.SharedPreferenceBooleanLiveData(prefs, key, value)
//    }
//
//    protected fun observeValue(
//        key: String,
//        value: Int
//    ): SharedPreferenceLiveData.SharedPreferenceIntegerLiveData {
//        return SharedPreferenceLiveData.SharedPreferenceIntegerLiveData(prefs, key, value)
//    }
}

@ExperimentalCoroutinesApi
val SharedPreferences.keyFlow
    get() = callbackFlow {
        val listener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key: String? -> trySend(key) }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
    }

internal typealias KeyFlow = Flow<String?>