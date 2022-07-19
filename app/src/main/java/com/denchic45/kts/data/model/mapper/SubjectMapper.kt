package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.domain.model.Subject
import com.denchic45.kts.data.remote.model.SubjectDoc
import com.denchic45.kts.data.model.room.SubjectEntity
import org.mapstruct.Mapper

@Mapper
abstract class SubjectMapper {

    abstract fun docToEntity(doc: SubjectDoc): SubjectEntity

    abstract fun docToEntity(doc: List<SubjectDoc>): List<SubjectEntity>

    abstract fun domainToDoc(domain: Subject): SubjectDoc

    abstract fun domainToEntity(domain: MutableList<Subject>): MutableList<SubjectEntity>

    abstract fun entityToDomain(entity: SubjectEntity): Subject

    abstract fun entityToDomain(entities: List<SubjectEntity>): List<Subject>

    abstract fun docToDomain(subjectDoc: SubjectDoc): Subject

    abstract fun docToDomain(list: List<SubjectDoc>):List<Subject>
}