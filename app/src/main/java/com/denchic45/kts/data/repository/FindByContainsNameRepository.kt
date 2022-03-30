package com.denchic45.kts.data.repository

import com.denchic45.kts.data.model.DocModel
import kotlinx.coroutines.flow.Flow

interface FindByContainsNameRepository<D : DocModel> {

    fun findByContainsName(text: String): Flow<List<D>>
}