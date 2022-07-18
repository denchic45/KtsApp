package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.domain.model.Specialty
import com.denchic45.kts.data.model.firestore.SpecialtyDoc
import com.denchic45.kts.data.model.room.SpecialtyEntity
import org.mapstruct.Mapper

@Mapper
interface SpecialtyMapper {

    fun entityToDoc(entity: SpecialtyEntity): SpecialtyDoc

    fun entityToDoc(entity: List<SpecialtyEntity>): List<SpecialtyDoc>

    fun docToEntity(doc: SpecialtyDoc): SpecialtyEntity

    fun docToEntity(doc: List<SpecialtyDoc>): List<SpecialtyEntity>

    fun domainToDoc(domain: Specialty): SpecialtyDoc

    fun docToDomain(doc: SpecialtyDoc): Specialty

    fun docToDomain(doc: List<SpecialtyDoc>): List<Specialty>

    fun domainToDoc(domain: List<Specialty>): List<SpecialtyDoc>

    fun domainToEntity(domain: Specialty): SpecialtyEntity

    fun entityToDomain(entity: SpecialtyEntity): Specialty

    fun domainToEntity(domain: List<Specialty>): List<SpecialtyEntity>
}