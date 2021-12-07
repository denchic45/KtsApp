package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class GroupWithCourses(
    val group: Group,
    val courses: List<Course>
) : DomainModel()

