package com.denchic45.kts.data.mapper

import com.denchic45.kts.SubjectEntity
import com.denchic45.kts.data.remote.model.SubjectDoc
import com.denchic45.kts.domain.model.Subject

fun SubjectEntity.toDomain() = Subject(
    id = subject_id,
    name = name,
    iconUrl = icon_url,
    colorName = color_name
)

fun SubjectDoc.toEntity() = SubjectEntity(
    subject_id = id,
    name = name,
    icon_url = iconUrl,
    color_name = colorName
)

fun List<SubjectDoc>.docsToEntities() = map { it.toEntity() }

fun SubjectDoc.toDomain() = Subject(
    id = id,
    name = name,
    iconUrl = iconUrl,
    colorName = colorName
)