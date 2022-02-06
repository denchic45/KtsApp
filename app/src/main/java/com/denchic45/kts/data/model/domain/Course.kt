package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class Course(
    override var id: String,
    var name: String,
    var subject: Subject,
    var teacher: User,
    var groups: List<CourseGroup>,
) : DomainModel() {

    companion object {
        fun createEmpty() = Course()

    }

    private constructor() : this("", "", Subject.createEmpty(), User.createEmpty(), emptyList())
}

data class CourseHeader(
    override var id: String,
    var name: String,
    var subject: Subject,
    var teacher: User
): DomainModel()