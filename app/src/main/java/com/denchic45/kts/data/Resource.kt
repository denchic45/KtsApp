package com.denchic45.kts.data

sealed class Resource<out T> {
    object Loading : Resource<Nothing>()
    data class Next<T>(val data: T, val status: String = "") : Resource<T>()
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val error: Exception) : Resource<Nothing>()
}