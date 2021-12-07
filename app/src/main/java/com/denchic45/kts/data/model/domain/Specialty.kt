package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel

data class Specialty(
    override var uuid: String,
    val name: String
) : DomainModel() {

    private constructor() : this("", "")


    override fun copy(): Specialty {
        return Specialty(uuid, name)
    }


    companion object {
        fun createEmpty(): Specialty {
            return Specialty()
        }
    }
}