package com.denchic45.kts.data.model

abstract class DomainModel() : Equatable {

    open var uuid: String = ""

    open fun copy(): DomainModel? {
        return null
    }
}
