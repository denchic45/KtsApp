package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class CourseSection(
    override var uuid: String,
    val name: String
) : DomainModel()
