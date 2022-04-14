package com.denchic45.kts.data.model.mapper

import com.denchic45.kts.data.model.domain.GroupCurator
import com.denchic45.kts.data.model.domain.GroupMembers
import com.denchic45.kts.data.model.domain.GroupStudent
import com.denchic45.kts.data.model.room.GroupWithCuratorAndStudentsEntity
import com.denchic45.kts.data.model.room.UserEntity
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(uses = [UserMapper::class, SpecialtyMapper::class])
abstract class GroupMemberMapper {

    @Mapping(source = "groupEntity.id", target = "groupId")
    @Mapping(source = "studentEntities", target = "students")
    @Mapping(source = "curatorEntity", target = "curator")
    fun entityToDomainGroupMembers(groupWithCuratorAndStudentsEntities: GroupWithCuratorAndStudentsEntity): GroupMembers {
        return GroupMembers(
            groupId = groupWithCuratorAndStudentsEntities.groupEntity.id,
            curator = userEntityToGroupCurator(groupWithCuratorAndStudentsEntities.curatorEntity),
            students = groupWithCuratorAndStudentsEntities.studentEntities.map {
                userEntityToGroupStudent(it)
            },
            headmanId = groupWithCuratorAndStudentsEntities.groupEntity.headmanId
        )
    }

    abstract fun userEntityToGroupStudent(userEntity: UserEntity): GroupStudent

    abstract fun userEntityToGroupCurator(userEntity: UserEntity): GroupCurator
}