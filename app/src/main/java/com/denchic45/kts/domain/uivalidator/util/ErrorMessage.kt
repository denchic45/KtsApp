package com.denchic45.kts.domain.uivalidator.util

sealed class ErrorMessage {

    data class Resource(val value: Int) : ErrorMessage()

    data class Stroke(val value: String) : ErrorMessage()

    val isString get() = this is Stroke

    val isId get() = this is Resource

    fun fold(fnL: (Int) -> Any, fnR: (String) -> Any): Any =
        when (this) {
            is Resource -> fnL(value)
            is Stroke -> fnR(value)
        }
}

fun ErrorMessage.onStroke(fn: (success: String) -> Unit): ErrorMessage =
    this.apply { if (this is ErrorMessage.Stroke) fn(value) }

fun ErrorMessage.onResource(fn: (failure: Int) -> Unit): ErrorMessage =
    this.apply { if (this is ErrorMessage.Resource) fn(value) }