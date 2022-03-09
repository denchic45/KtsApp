package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.CourseGroup
import com.denchic45.kts.data.model.domain.Group
import com.denchic45.kts.data.model.domain.Group.Companion.deleted
import com.denchic45.kts.data.model.firestore.GroupDoc
import com.denchic45.kts.data.model.room.GroupEntity
import com.denchic45.kts.data.model.room.GroupWithCuratorAndSpecialtyEntity
import com.google.firebase.firestore.FieldValue
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(uses = [UserMapper::class, SpecialtyMapper::class])
abstract class GroupMapper {
    @Mapping(target = "searchKeys", ignore = true)
    @Mapping(target = "allUsers", ignore = true)
    @Mapping(target = "students", ignore = true)
    abstract fun domainToDoc(domain: Group): GroupDoc

    @DoIgnore
    @Mapping(target = "specialty", source = "specialtyEntity")
    @Mapping(target = ".", source = "groupEntity")
    @Mapping(target = "curator", source = "curatorEntity")
    abstract fun groupWithCuratorAndSpecialtyEntityToGroup(entity: GroupWithCuratorAndSpecialtyEntity?): Group
    fun entityToDomain(entity: GroupWithCuratorAndSpecialtyEntity?): Group {
        return entity?.let { groupWithCuratorAndSpecialtyEntityToGroup(it) }
            ?: deleted()
    }

    fun domainToMap(group: Group): Map<String, Any> {
        val map: MutableMap<String, Any> = HashMap()
        map["id"] = group.id
        map["name"] = group.name
        map["course"] = group.course
        map["specialtyId"] = group.specialty.id
        map["timestamp"] = FieldValue.serverTimestamp()
        map["specialty"] = group.specialty
        map["curator"] = group.curator
        return map
    }

    @Mapping(source = "specialty.id", target = "specialtyId")
    abstract fun docToCourseGroupDomain(groupDoc: GroupDoc): CourseGroup

    abstract fun docToCourseGroupDomain(groupDocs: List<GroupDoc?>?): List<CourseGroup>

    @Mapping(source = "curator.id", target = "curatorId")
    @Mapping(source = "specialty.id", target = "specialtyId")
    abstract fun docToEntity(doc: GroupDoc): GroupEntity
}