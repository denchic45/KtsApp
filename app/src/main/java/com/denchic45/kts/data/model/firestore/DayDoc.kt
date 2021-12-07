package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.room.TaskEntity
import com.denchic45.kts.utils.UUIDS
import com.google.firebase.firestore.ServerTimestamp
import java.util.*
import java.util.stream.Collectors

data class DayDoc(
    val uuid: String = UUIDS.createShort(),
    var date: Date? = null,
    private var _events: List<EventDoc> = emptyList(),
    var homework: List<TaskEntity> = emptyList(),
    var teacherIds: List<String> = emptyList(),
    var subjectIds: List<String> = emptyList(),
    @ServerTimestamp
    var timestamp: Date? = null,
    var groupUuid: String = ""

) : DocModel {

    var events: List<EventDoc>
        set(value) {
            _events = value
            teacherIds = events.stream()
                .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == "LESSON" }
                .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.teacherUuidList }
                .flatMap { obj: List<String> -> obj.stream() }
                .collect(Collectors.toList())
            subjectIds = events.stream()
                .filter { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.type == "LESSON" }
                .map { eventDoc: EventDoc -> eventDoc.eventDetailsDoc.subjectUuid }
                .collect(Collectors.toList())
        }
        get() = _events
}