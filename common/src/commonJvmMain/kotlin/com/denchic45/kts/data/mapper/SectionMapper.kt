package com.denchic45.kts.data.mapper

import com.denchic45.kts.SectionEntity
import com.denchic45.kts.data.remote.model.SectionDoc
import com.denchic45.kts.domain.model.Section

@Deprecated("")
fun SectionDoc.toEntity() = SectionEntity(
    section_id = id,
    course_id = courseId,
    name = name,
    order = order
)

@Deprecated("")
fun List<SectionDoc>.docsToEntities() = map { it.toEntity() }

fun SectionEntity.toDomain() = Section(
    courseId = course_id,
    name = name,
    order = order,
    id = section_id
)

fun List<SectionEntity>.entitiesToDomains() = map { it.toDomain() }