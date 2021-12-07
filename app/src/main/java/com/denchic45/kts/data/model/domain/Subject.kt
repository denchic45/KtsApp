package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.model.mapper.Default

data class Subject(
    override var uuid: String,
    val name: String,
    val iconUrl: String,
    val colorName: String
) : DomainModel() {

    val isEmpty: Boolean
    get() = uuid.isEmpty()

    private constructor():this("","","","")

    override fun copy(): Subject {
        return Subject(uuid, name, iconUrl, colorName)
    }

    companion object {
        @JvmStatic
        fun createEmpty(): Subject {
            return Subject()
        }
    }
}