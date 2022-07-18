package com.denchic45.kts.data.model.domain

import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.domain.model.GroupHeader

data class GroupCourses(
    val groupHeader: GroupHeader,
    val courses: List<CourseHeader>
) : DomainModel {

    override val id: String = groupHeader.id
}

