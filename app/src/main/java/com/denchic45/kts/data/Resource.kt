package com.denchic45.kts.data

sealed class Resource2<out T> {
    object Loading : Resource2<Nothing>()
    data class Next<T>(val data: T, val status: String = "") : Resource2<T>()
    data class Success<T>(val data: T) : Resource2<T>()
    data class Error(val error: Exception) : Resource2<Nothing>()
}