package com.denchic45.kts.domain.model

import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.domain.model.GroupHeader
import com.denchic45.kts.domain.model.User

data class Course(
    override var id: String,
    val name: String,
    val subject: Subject,
    val teacher: User,
    val groupHeaders: List<GroupHeader>,
) : DomainModel {

    companion object {
        fun createEmpty() = Course()
    }

    private constructor() : this("", "", Subject.createEmpty(), User.createEmpty(), emptyList())
}

data class CourseHeader(
    override var id: String,
    val name: String,
    val subject: Subject,
    val teacher: User
): DomainModel