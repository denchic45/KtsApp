package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.room.EventEntity.TYPE

class EmptyEventDetails : EventDetails() {
    override val type: TYPE
        get() = TYPE.EMPTY

    override fun equals(other: Any?): Boolean {
        return (other as EventDetails?)!!.type == TYPE.EMPTY
    }

    override fun hashCode(): Int {
        return type.hashCode()
    }
}