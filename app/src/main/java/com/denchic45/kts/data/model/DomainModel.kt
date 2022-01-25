package com.denchic45.kts.data.model

abstract class DomainModel : Equatable {

    open var id: String = ""

    override fun equals(other: Any?): Boolean = throw IllegalStateException()

    override fun hashCode(): Int = throw java.lang.IllegalStateException()

    open fun copy(): DomainModel {
        throw IllegalStateException("You must override copy function")
    }
}
