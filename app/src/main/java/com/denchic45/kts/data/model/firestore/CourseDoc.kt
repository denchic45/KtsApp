package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class CourseDoc(
    var uuid: String,
    var name: String? = null,
    var subject: SubjectDoc? = null,
    var teacher: UserDoc? = null,
    var groupUuids: List<String>? = null
) : DocModel {
    fun equalSubjectsAndTeachers(oldCourseDoc: CourseDoc) =
        oldCourseDoc.subject != subject
                && oldCourseDoc.teacher != teacher

    var searchKeys: List<String>? = null

    @ServerTimestamp
    val timestamp: Date? = null
    private constructor(): this("", null, null)
}
