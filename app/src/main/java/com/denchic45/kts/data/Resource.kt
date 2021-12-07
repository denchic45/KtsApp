package com.denchic45.kts.data

class Resource<out T> {
    val data: T
    var status: String? = null
        private set
    var error: Exception? = null
        private set

    constructor(data: T, status: String?, error: Exception?) {
        this.status = status
        this.data = data
        this.error = error
    }

    constructor(data: T, status: String?) {
        this.status = status
        this.data = data
        error = null
    }

    fun <T> setData(data: T): Resource<T> {
        return Resource(data, status, error)
    }

    val isSuccessful: Boolean
        get() = status == SUCCESSFUL
    val isLoading: Boolean
        get() = status == LOADING

    val isError: Boolean
    get() = status == ERROR

    companion object {
        const val SUCCESSFUL = "SUCCESSFUL"
        const val LOADING = "LOADING"
        const val ERROR = "ERROR"

        fun <T> successful(data: T): Resource<T> {
            return Resource(data, SUCCESSFUL, null)
        }

        fun <T> error(exception: Exception): Resource<T?> {
            return Resource(null, ERROR, exception)
        }

        fun <T> error(data: T, error: Exception): Resource<T> {
            return Resource(data, ERROR, error)
        }
    }
}

sealed class Resource2<out T> {
    object Loading : Resource2<Nothing>()
    data class Next<T>(val data: T, val status: String = "") : Resource2<T>()
    data class Success<T>(val data: T) : Resource2<T>()
    data class Error(val error: Exception) : Resource2<Nothing>()
}