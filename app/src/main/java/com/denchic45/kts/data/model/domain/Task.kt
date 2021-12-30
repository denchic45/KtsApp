package com.denchic45.kts.data.model.domain

import com.denchic45.kts.data.model.DomainModel
import java.io.File
import java.util.*

data class Task(
    override var uuid: String,
    val courseUuid: String,
    val sectionUuid: String,
    val name: String,
    val content: String,
    val attachments: List<Attachment>,
    val completionDate: Date,
    val createdDate: Date,
    val updatedDate: Date,
    val completed: Boolean,
) : DomainModel() {

    private constructor() : this("", "", "", "", "", emptyList(), Date(0), Date(0), Date(), false)

    companion object {
        fun createEmpty() = Task()
    }
}

data class Attachment(val file: File) : DomainModel()