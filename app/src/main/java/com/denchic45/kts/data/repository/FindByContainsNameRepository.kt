package com.denchic45.kts.data.repository

import com.denchic45.kts.data.model.DocModel
import com.denchic45.kts.data.model.DomainModel
import kotlinx.coroutines.flow.Flow

interface FindByContainsNameRepository<D : DomainModel> {

    fun findByContainsName(text: String): Flow<List<D>>
}