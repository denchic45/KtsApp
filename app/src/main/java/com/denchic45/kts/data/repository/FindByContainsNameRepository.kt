package com.denchic45.kts.data.repository

import com.denchic45.kts.data.model.DomainModel
import kotlinx.coroutines.flow.Flow

interface FindByContainsNameRepository<T : DomainModel> {

    fun findByContainsName(text: String): Flow<List<T>>
}