package com.denchic45.kts.data.model.firestore

import com.denchic45.kts.data.model.DocModel
import java.util.*

data class TaskDoc(
    val uuid:String,
    val courseUuid:String,
    val name:String,
    val content: String,
    val createdDate: Date,
    val updatedDate: Date,
    val completionDate: Date,
    val completions: List<CompletionTask>
):DocModel

data class CompletionTask(
    val studentUuid: String,
    val teacherUuid: String,
    val completedDate: Date,
    val grade:Int,
    val assessmentDate: Date,
)