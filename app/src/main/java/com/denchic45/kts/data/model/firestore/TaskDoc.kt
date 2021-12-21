package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.utils.UUIDS
import java.util.*

data class TaskDoc(
    val uuid:String = "",
    val courseUuid:String ="",
    val name:String = "",
    val content: String = "",
    val createdDate: Date = Date(),
    val updatedDate: Date = Date(),
    val completionDate: Date = Date(),
    val completions: List<CompletionTask> = emptyList()
):DocModel

data class CompletionTask(
    val studentUuid: String,
    val teacherUuid: String,
    val completedDate: Date,
    val grade:Int,
    val assessmentDate: Date,
)