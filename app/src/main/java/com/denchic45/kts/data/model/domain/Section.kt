package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class Section(
    override var id: String,
    val courseId: String,
    val name: String,
    val order: Int
) : DomainModel() {
    companion object {
        fun createEmpty() = Section("", "", "", -1)
    }
}