package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class GroupCourses(
    val group: CourseGroup,
    val courses: List<CourseHeader>
) : DomainModel()

