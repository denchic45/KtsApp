package com.denchic45.kts.data.model

 interface DomainModel : Equatable {

     val id: String

    fun copy(): DomainModel {
        throw IllegalStateException("You must override copy function")
    }
}
