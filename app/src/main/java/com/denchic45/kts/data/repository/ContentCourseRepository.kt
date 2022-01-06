package com.denchic45.kts.data.repository

import android.content.Context
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import com.denchic45.kts.data.model.domain.Task
import com.denchic45.kts.data.model.mapper.TaskMapper
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ContentCourseRepository @Inject constructor(
    context: Context,
    override val networkService: NetworkService,
    private val firestore: FirebaseFirestore,
    private val taskMapper: TaskMapper
) : Repository(context) {

   suspend fun add(task: Task) {
       checkInternetConnection()
         firestore.document(task.courseId)
            .collection("Contents")
            .document(task.uuid)
            .set(taskMapper.domainToDoc(task))
             .await()
    }
}