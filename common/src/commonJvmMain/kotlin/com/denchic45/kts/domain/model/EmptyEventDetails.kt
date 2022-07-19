package com.denchic45.kts.domain.model

class EmptyEventDetails : EventDetails() {
    override val type: Event.TYPE
        get() = Event.TYPE.EMPTY

    override fun equals(other: Any?): Boolean {
        return (other as EventDetails?)!!.type == Event.TYPE.EMPTY
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}