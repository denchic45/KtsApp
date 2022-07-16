package com.denchic45.kts.data.model.domain

import com.denchic45.kts.domain.DomainModel

data class Subject(
    override var id: String,
    val name: String,
    val iconUrl: String,
    val colorName: String
) : DomainModel {

    val isEmpty: Boolean
    get() = id.isEmpty()

    private constructor():this("","","","")

    override fun copy(): Subject {
        return Subject(id, name, iconUrl, colorName)
    }

    companion object {

        fun createEmpty(): Subject {
            return Subject()
        }
    }
}