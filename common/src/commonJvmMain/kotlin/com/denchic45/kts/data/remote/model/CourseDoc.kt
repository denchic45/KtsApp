package com.denchic45.kts.data.remote.model

import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.util.SearchKeysGenerator
import java.util.*

data class CourseDoc(
    var id: String,
    var name: String,
    val sections: List<SectionDoc>?,
    var subject: SubjectDoc,
    var teacher: UserDoc,
    var groupIds: List<String>
) : DocModel {
    fun equalSubjectsAndTeachers(oldCourseDoc: CourseDoc) =
        oldCourseDoc.subject != subject
                && oldCourseDoc.teacher != teacher

    val searchKeys: List<String>
        get() = SearchKeysGenerator().generateKeys(name) { predicate: String -> predicate.length > 1 }

    val timestamp: Date? = null

    private constructor() : this(
        "", "", emptyList(),
        SubjectDoc.createEmpty(), UserDoc.createEmpty(), emptyList()
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
