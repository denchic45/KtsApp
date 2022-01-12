package com.denchic45.kts.data.prefs

import android.content.Context
import javax.inject.Inject

class CoursePreference @Inject constructor(context: Context) :
    BaseSharedPreference(context, "Courses") {

    fun getTimestampContentsOfCourse(courseId: String): Long {
        return getValue(COURSE_CONTENT_TIMESTAMP_ + courseId, 0L)
    }

    fun setTimestampContentsOfCourse(courseId: String, timestamp: Long) {
        setValue(COURSE_CONTENT_TIMESTAMP_ + courseId, timestamp)
    }

    companion object {
        const val COURSE_CONTENT_TIMESTAMP_ = "COURSE_CONTENT_TIMESTAMP_"
    }
}