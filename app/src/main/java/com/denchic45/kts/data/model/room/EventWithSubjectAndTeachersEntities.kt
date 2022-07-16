package com.denchic45.kts.data.model.room

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.denchic45.kts.domain.EntityModel

data class EventWithSubjectAndTeachersEntities(
    @Embedded
    var eventEntity: EventEntity,

//    @Relation(parentColumn = "date", entityColumn = "completion_date")
//    var courseContentEntity: CourseContentEntity,

    @Relation(parentColumn = "subject_id", entityColumn = "subject_id")
    var subjectEntity: SubjectEntity?,

    @Relation(entity = GroupEntity::class, parentColumn = "group_id", entityColumn = "group_id")
    var groupEntity: GroupWithCuratorAndSpecialtyEntity,

    @Relation(
        parentColumn = "event_id", entityColumn = "user_id", associateBy = Junction(
            TeacherEventCrossRef::class
        ), entity = UserEntity::class
    )
    var teacherEntities: List<UserEntity>?
) : EntityModel