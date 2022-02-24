package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.room.CourseContentEntity
import com.denchic45.kts.utils.UUIDS
import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import java.util.stream.Collectors

data class DayDoc(
    val id: String = UUIDS.createShort(),
    var date: Date,
    private var _events: List<EventDoc>,
    var homework: List<CourseContentEntity> = emptyList(),
    var teacherIds: List<String> = emptyList(),
    var subjectIds: List<String> = emptyList(),
    @ServerTimestamp
    var timestamp: Date? = null,
    var groupId: String
) : DocModel {

    private constructor(): this("", Date(), emptyList(), emptyList(), emptyList(), emptyList(), Date(), "")

    var events: List<EventDoc>
        set(value) {
            _events = value
            teacherIds = events.stream()
                .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == "LESSON" }
                .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.teacherIds }
                .flatMap { obj: List<String> -> obj.stream() }
                .collect(Collectors.toList())
            subjectIds = events.stream()
                .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == "LESSON" }
                .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.subjectId }
                .collect(Collectors.toList())
        }
        get() = _events
}