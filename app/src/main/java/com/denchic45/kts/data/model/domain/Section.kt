package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.utils.UUIDS

data class Section(
    val courseId: String,
    val name: String,
    val order: Long,
    override var id: String = UUIDS.createShort()
) : DomainModel() {
    companion object {
        fun createEmpty() = Section("", "", -1, "")
    }
}