package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.room.EventEntity.TYPE

abstract class EventDetails : DomainModel() {
    abstract val type: TYPE
    override fun copy(): EventDetails {
        return EmptyEventDetails()
    }
}