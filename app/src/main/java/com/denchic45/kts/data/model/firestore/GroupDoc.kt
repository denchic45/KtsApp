package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class GroupDoc(
    var id: String,
    var course: Int,
    var name: String,
    var curator: UserDoc? = null,
    @ServerTimestamp
    var timestamp: Date? = null,
    var timestampCourses: Date? = null,
    var specialty: SpecialtyDoc? = null,
) : DocModel {

    private constructor() : this("", 0, "")

    var searchKeys: List<String>? = null

    var students: Map<String, UserDoc>? = null
        get() = field ?: emptyMap()
    var subjects: Map<String, SubjectDoc>? = null
        get() = field ?: emptyMap()
    var teachers: Map<String, UserDoc>? = null
        get() = field ?: emptyMap()
    var courses: List<SubjectTeacherPair>? = null
        get() = field ?: emptyList()

    var teacherIds: List<String>? = null
        get() = field ?: emptyList()


    @get:Exclude
    val allUsers: List<UserDoc?>
        get() = students!!.values + (curator)
}

data class SubjectTeacherPair(
    var subjectId: String,
    var teacherId: String
) {
    private constructor() : this("", "")
}