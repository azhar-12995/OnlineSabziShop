package com.azhar.sabzishop.utils

/**
 * A generic wrapper class to represent the state of a resource (data operation).
 * Used across repository, use case, and ViewModel layers.
 */
sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(val message: String) : Resource<Nothing>()
    object Loading : Resource<Nothing>()
}

