package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class CourseDoc(
    var id: String,
    var name: String,
    val sections: List<SectionDoc>,
    var subject: SubjectDoc,
    var teacher: UserDoc,
    var groupIds: List<String>
) : DocModel {
    fun equalSubjectsAndTeachers(oldCourseDoc: CourseDoc) =
        oldCourseDoc.subject != subject
                && oldCourseDoc.teacher != teacher

    var searchKeys: List<String>? = null

    @ServerTimestamp
    val timestamp: Date? = null

    private constructor() : this(
        "", "", emptyList(),
        SubjectDoc(), UserDoc.createEmpty(), emptyList()
    )
}

data class SectionDoc(
    val id: String,
    val courseId: String,
    val name: String,
    val order: Int
) : DocModel {
    private constructor() : this("", "", "", 0)
}
