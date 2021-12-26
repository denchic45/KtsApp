package com.denchic45.kts.data.prefs

import android.content.Context
import javax.inject.Inject

class AppPreference @Inject constructor(context: Context) : BaseSharedPreference(context, "App") {
    var lessonTime: Int
        get() = getValue(LESSON_TIME, 40)
        set(value) {
            setValue(LESSON_TIME, value)
        }

    var coursesLoadedFirstTime: Boolean
        get() = getValue(COURSES_LOADED_FIRST_TIME, false)
        set(value) {
            setValue(COURSES_LOADED_FIRST_TIME, value)
        }

    companion object {
        const val LESSON_TIME = "LESSON_TIME"
        const val COURSES_LOADED_FIRST_TIME = "COURSES_LOADED_FIRST_TIME"
    }
}