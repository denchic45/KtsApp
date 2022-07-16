package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.data.model.mapper.Default
import com.denchic45.kts.data.model.room.EventEntity
import com.denchic45.kts.util.UUIDS
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class DayDoc(
    val id: String,
    var date: Date,
    var startsAtZero: Boolean,
    var events: List<EventDoc>,
    var groupId: String
) : DocModel {

    @Default
    constructor(
        date: Date,
        startsAtZero: Boolean,
        events: List<EventDoc>,
        groupId: String
    ) : this(
        id = UUIDS.createShort(),
        date = date,
        startsAtZero = startsAtZero,
        events = events,
        groupId = groupId
    )

    private constructor() : this(
        "",
        Date(),
        false,
        emptyList(),
        ""
    )

    @ServerTimestamp
    var timestamp: Date? = null

    val teacherIds: List<String>
        get() = events
            .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == EventEntity.TYPE.LESSON }
            .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.teacherIds!! }
            .flatten()

    val subjectIds: List<String>
        get() = events
            .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == EventEntity.TYPE.LESSON }
            .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.subjectId!! }
}