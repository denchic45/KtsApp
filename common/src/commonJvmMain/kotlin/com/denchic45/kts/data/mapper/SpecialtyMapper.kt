package com.denchic45.kts.data.mapper

import com.denchic45.kts.SpecialtyEntity
import com.denchic45.kts.data.remote.model.SpecialtyDoc
import com.denchic45.kts.domain.model.Specialty

fun SpecialtyEntity.toDomain() = Specialty(
    id = specialty_id,
    name = name
)

fun SpecialtyDoc.toEntity() = SpecialtyEntity(
    specialty_id = id,
    name = name
)

fun SpecialtyDoc.toDomain() = Specialty(
    id = id,
    name = name
)