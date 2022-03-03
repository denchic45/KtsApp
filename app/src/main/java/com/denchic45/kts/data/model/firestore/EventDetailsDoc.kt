package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.room.EventEntity

data class EventDetailsDoc(
    var subjectId: String?,
    var teacherIds: List<String>?,
    var name: String?,
    var iconUrl: String,
    var color: String,
    var type: EventEntity.TYPE
) : DocModel {

    private constructor() : this("", emptyList(), "", "", "", EventEntity.TYPE.EMPTY)

    companion object {
        fun createEmpty() = EventDetailsDoc()
    }
}