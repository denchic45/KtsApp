package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.room.CourseContentEntity
import com.denchic45.kts.data.model.room.EventEntity
import com.denchic45.kts.utils.UUIDS
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class DayDoc(
    val id: String = UUIDS.createShort(),
    var date: Date,
    private var _events: List<EventDoc>,
    var teacherIds: List<String> = emptyList(),
    var subjectIds: List<String> = emptyList(),
    @ServerTimestamp
    var timestamp: Date? = null,
    var groupId: String
) : DocModel {

    private constructor() : this(
        "",
        Date(),
        emptyList(),
        emptyList(),
        emptyList(),
        Date(),
        ""
    )

    var events: List<EventDoc>
        set(value) {
            _events = value
            teacherIds = events
                .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == EventEntity.TYPE.LESSON }
                .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.teacherIds!! }
                .flatten()

            subjectIds = events
                .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == EventEntity.TYPE.LESSON }
                .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.subjectId!! }
        }
        get() = _events
}