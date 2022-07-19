package com.denchic45.kts.domain.model

import com.denchic45.kts.domain.DomainModel

abstract class EventDetails : DomainModel {
    abstract val type: Event.TYPE
    override fun copy(): EventDetails {
        return EmptyEventDetails()
    }

    override val id: String = ""
}