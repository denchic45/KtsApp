package com.denchic45.kts.data.model.room

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.Relation
import com.denchic45.kts.data.model.EntityModel

data class EventTaskSubjectTeachersEntities(
    @Embedded
    var eventEntity: EventEntity?,

    @Relation(parentColumn = "date", entityColumn = "date_completion")
    var courseContentEntity: CourseContentEntity?,

    @Relation(parentColumn = "subject_id", entityColumn = "subject_id")
    var subjectEntity: SubjectEntity?,

    @Relation(entity = GroupEntity::class, parentColumn = "group_id", entityColumn = "group_id")
    var groupEntity: GroupWithCuratorAndSpecialtyEntity?,

    @Relation(
        parentColumn = "eventUuid", entityColumn = "uuid_user", associateBy = Junction(
            TeacherEventCrossRef::class
        ), entity = UserEntity::class
    )
    var teacherEntities: List<UserEntity>?
) : EntityModel