package com.denchic45.kts.data.model

abstract class DomainModel : Equatable {

    open var id: String = ""

    open fun copy(): DomainModel {
        throw IllegalStateException("You must override copy function")
    }
}
