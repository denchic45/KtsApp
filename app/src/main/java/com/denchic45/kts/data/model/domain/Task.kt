package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import java.util.*

data class Task(
    override var uuid: String,
    val courseUuid: String,
    val sectionUuid:String,
    val name: String,
    val content: String,
    val completionDate: Date,
    val createdDate: Date,
    val updatedDate: Date,
    val completed: Boolean,
) : DomainModel() {

    private constructor():this("","", "","","",Date(0),Date(0), Date(),false)

    companion object {
        fun createEmpty() = Task()
    }
}