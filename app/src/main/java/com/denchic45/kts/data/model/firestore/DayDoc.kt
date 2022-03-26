package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.room.EventEntity
import com.denchic45.kts.utils.UUIDS
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class DayDoc(
    val id: String = UUIDS.createShort(),
    var date: Date,
    var startAtZero:Boolean,
    private var _events: List<EventDoc>,
    @ServerTimestamp
    var timestamp: Date? = null,
    var groupId: String
) : DocModel {

    private constructor() : this(
        "",
        Date(),
        false,
        emptyList(),
        Date(),
        ""
    )

    var events: List<EventDoc>
        set(value) {
            _events = value
        }
        get() = _events

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