package com.example.dietapp.domain.util

/**
 * Generic wrapper for API/database results.
 * Follows the single source of truth pattern.
 */
sealed class Resource<out T> {

    data class Success<out T>(val data: T) : Resource<T>()

    data class Error<out T>(
        val message: String,
        val data: T? = null,
        val throwable: Throwable? = null
    ) : Resource<T>()

    data class Loading<out T>(val data: T? = null) : Resource<T>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    /**
     * Returns the data if available, null otherwise.
     */
    fun dataOrNull(): T? = when (this) {
        is Success -> data
        is Error -> data
        is Loading -> data
    }

    /**
     * Maps the data if this is a Success.
     */
    fun <R> map(transform: (T) -> R): Resource<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> Error(message, data?.let(transform), throwable)
        is Loading -> Loading(data?.let(transform))
    }

    companion object {
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> error(message: String, data: T? = null, throwable: Throwable? = null): Resource<T> =
            Error(message, data, throwable)
        fun <T> loading(data: T? = null): Resource<T> = Loading(data)
    }
}
