package com.denchic45.kts.data.model.domain

import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.util.UUIDS

data class Section(
    val courseId: String,
    val name: String,
    val order: Long,
    override var id: String = UUIDS.createShort()
) : DomainModel {
    companion object {
        fun createEmpty() = Section("", "", -1, "")
    }
}