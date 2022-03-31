package com.denchic45.kts.domain.usecase

import com.denchic45.kts.data.Resource
import com.denchic45.kts.data.model.DomainModel
import com.denchic45.kts.data.repository.FindByContainsNameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest

abstract class FindByContainsNameUseCase<T : DomainModel>(
    private val repository: FindByContainsNameRepository<T>
) {
    operator fun invoke(name: String): Flow<Resource<List<T>>> = flow {
        try {
            emitAll(repository.findByContainsName(name).mapLatest { Resource.Success(it) })
        } catch (t: Throwable) {
            emit(Resource.Error(t))
        }
    }
}