package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class Course(
    val info: CourseInfo,
    var groups: List<Group>,
):DomainModel() {

    override var uuid: String
        get() = info.uuid
        set(value) {info.uuid = value}

    private constructor():this(CourseInfo("","", Subject.createEmpty(), User.createEmpty()), mutableListOf())

    companion object {
        @JvmStatic
        fun createEmpty(): Course {
            return Course()
        }
    }
}

data class CourseInfo(
    override var uuid: String,
    val name: String,
    var subject: Subject,
    var teacher: User
) : DomainModel() {
    fun equalSubjectsAndTeachers(oldCourseInfo: CourseInfo) =
        oldCourseInfo.subject != subject
                && oldCourseInfo.teacher != teacher
}