package com.denchic45.kts.data.remote.model

import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.domain.model.Event
import com.denchic45.kts.util.UUIDS
import java.util.*

data class DayDoc(
    val id: String,
    val date: Date,
    val startsAtZero: Boolean,
    var events: List<EventDoc>,
    val groupId: String
) : DocModel {

    private constructor() : this(
        "",
        Date(),
        false,
        emptyList(),
        ""
    )

    var timestamp: Date? = null

    val teacherIds: List<String>
        get() = events
            .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == Event.TYPE.LESSON }
            .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.teacherIds!! }
            .flatten()

    val subjectIds: List<String>
        get() = events
            .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == Event.TYPE.LESSON }
            .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.subjectId!! }
}