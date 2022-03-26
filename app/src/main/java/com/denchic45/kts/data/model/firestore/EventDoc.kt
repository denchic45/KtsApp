package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import java.util.*

data class EventDoc(
    var id: String,
    var date: Date,
    var room: String,
    var groupId: String,
    var eventDetailsDoc: EventDetailsDoc
) : DocModel {

    private constructor() : this("", Date(), "", "", EventDetailsDoc.createEmpty())

    companion object {
        fun createEmpty() = EventDoc()
    }
}