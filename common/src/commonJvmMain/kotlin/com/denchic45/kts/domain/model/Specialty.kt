package com.denchic45.kts.domain.model

import com.denchic45.kts.domain.DomainModel

data class Specialty(
    override var id: String,
    val name: String
) : DomainModel {

    private constructor() : this("", "")


    override fun copy(): Specialty {
        return Specialty(id, name)
    }


    companion object {
        fun createEmpty(): Specialty {
            return Specialty()
        }
    }
}