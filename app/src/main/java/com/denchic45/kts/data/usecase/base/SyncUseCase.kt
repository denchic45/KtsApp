package com.denchic45.kts.data.usecase.base

abstract class SyncUseCase<out T, P:Any> {

    abstract operator fun invoke(params: P? = null): T
}