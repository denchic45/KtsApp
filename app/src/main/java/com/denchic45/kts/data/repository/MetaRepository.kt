package com.denchic45.kts.data.repository

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import com.denchic45.kts.data.prefs.AppPreference
import com.google.firebase.storage.FirebaseStorage
import org.jetbrains.annotations.Contract
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject

class MetaRepository @Inject constructor(context: Context) {
    private val oneMegabyte = (1024 * 1024).toLong()
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val appPreference: AppPreference = AppPreference(context)
    private val metaData: Unit
        get() {
            storage.getReference("meta.json")
                .getBytes(oneMegabyte)
                .addOnSuccessListener { bytes: ByteArray ->
                    val meta = tryCreateJson(bytes)
                    appPreference.lessonTime = tryGetInt(meta, LESSON_TIME)
                }
                .addOnFailureListener { e: Exception? -> Log.d("lol", "onFailure: ", e) }
        }
    val lessonTime: LiveData<Int>
        get() = appPreference.observeValueLiveData(AppPreference.LESSON_TIME, 50)

    private fun tryGetInt(meta: JSONObject, key: String): Int {
        try {
            return meta.getInt(key)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return 0
    }

    @Contract("_ -> new")
    private fun tryCreateJson(bytes: ByteArray): JSONObject {
        try {
            return JSONObject(String(bytes))
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        throw IllegalStateException()
    }

    companion object {
        const val LESSON_TIME = "lesson_time"
    }

    init {
        metaData
    }
}