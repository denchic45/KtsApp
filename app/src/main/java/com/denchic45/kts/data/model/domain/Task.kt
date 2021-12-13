package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import java.util.*

data class Task(
    override var uuid: String,
    val courseId: String,
    val sectionUuid:String,
    val name: String,
    val content: String,
    val dateOfCompletion: Date,
    val dateOfCreated: Date,
    val completed: Boolean,
) : DomainModel() {

    private constructor():this("","", "","","",Date(0),Date(0),false)

    companion object {
        fun createEmpty() = Task()
    }
}