package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.Subject
import com.denchic45.kts.data.model.firestore.SubjectDoc
import com.denchic45.kts.data.model.room.SubjectEntity
import org.mapstruct.Mapper

@Mapper
abstract class SubjectMapper {

    abstract fun docToEntity(doc: SubjectDoc?): SubjectEntity

    abstract fun domainToDoc(domain: Subject): SubjectDoc

    abstract fun domainToEntity(domain: MutableList<Subject>): MutableList<SubjectEntity>

    abstract fun entityToDomain(entity: SubjectEntity): Subject

    abstract fun entityToDomain(entities: List<SubjectEntity>): List<Subject>
}