package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel

data class EventDetailsDoc(
    var subjectId: String,
    var teacherIds: List<String>,
    var name: String,
    var iconUrl: String,
    var color: String,
    var type: String
) : DocModel {

    private constructor() : this("", emptyList(), "", "", "", "")

    companion object {
        fun createEmpty() = EventDetailsDoc()
    }
}