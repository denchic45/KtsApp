package com.denchic45.kts.domain.model

import com.denchic45.kts.domain.DomainModel
import com.denchic45.kts.util.UUIDS

data class Section(
    val courseId: String,
    val name: String,
    val order: Int,
    override var id: String = UUIDS.createShort()
) : DomainModel {
    companion object {
        fun createEmpty() = Section("", "", -1, "")
    }
}