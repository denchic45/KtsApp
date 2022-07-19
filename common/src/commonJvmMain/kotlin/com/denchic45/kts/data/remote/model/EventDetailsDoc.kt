package com.denchic45.kts.data.remote.model

import com.denchic45.kts.domain.DocModel
import com.denchic45.kts.domain.model.Event

data class EventDetailsDoc(
    var subjectId: String?,
    var teacherIds: List<String>?,
    var name: String?,
    var iconUrl: String?,
    var color: String?,
    var type: Event.TYPE
) : DocModel {

    private constructor() : this("", emptyList(), "", "", "", Event.TYPE.EMPTY)

    companion object {
        fun createEmpty() = EventDetailsDoc()
    }
}