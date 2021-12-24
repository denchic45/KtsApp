package com.denchic45.kts.data.usecase.base

abstract class UseCase<out T, P:Any> {

    abstract suspend operator fun invoke(params: P? = null): T
}