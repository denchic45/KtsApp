package com.denchic45.kts.data.remote.model

import com.denchic45.kts.domain.DocModel
import java.util.*

data class EventDoc(
    var id: String,
    var date: Date,
    var position:Int,
    var room: String,
    var groupId: String,
    var eventDetailsDoc: EventDetailsDoc
) : DocModel {

    private constructor() : this("", Date(), -1,"", "", EventDetailsDoc.createEmpty())

    companion object {
        fun createEmpty() = EventDoc()
    }
}