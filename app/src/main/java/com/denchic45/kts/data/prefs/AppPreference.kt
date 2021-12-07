package com.denchic45.kts.data.prefs

import android.content.Context
import javax.inject.Inject

class AppPreference @Inject constructor(context: Context) : BaseSharedPreference(context, "App") {
    var lessonTime: Int
        get() = getValue(LESSON_TIME, 40)
        set(lessonTime) {
            setValue(LESSON_TIME, lessonTime)
        }

    companion object {
        const val LESSON_TIME = "LESSON_TIME"
    }
}