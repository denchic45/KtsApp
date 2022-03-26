package com.denchic45.kts.data.model.room

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.denchic45.kts.data.model.EntityModel

data class DayWithEventsEntities(
    @Embedded
    var dayEntity: DayEntity,

    @Relation(
        entity = GroupEntity::class,
        parentColumn = "day_id",
        entityColumn = "day_id",
        associateBy = Junction(
            GroupCourseCrossRef::class
        )
    )
    var eventEntities: List<EventWithSubjectAndTeachersEntities>
) : EntityModel