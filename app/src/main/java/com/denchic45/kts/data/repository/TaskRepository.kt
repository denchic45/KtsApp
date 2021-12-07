package com.denchic45.kts.data.repository

import android.content.Context
import com.denchic45.kts.data.NetworkService
import com.denchic45.kts.data.Repository
import javax.inject.Inject

class TaskRepository @Inject constructor(
    context: Context,
    override val networkService: NetworkService
) : Repository(context) {}