package com.mmcl.hanapp.util

// A simple wrapper describing the outcome of a network call.
// Forces the UI to handle all three states explicitly, so loading spinners and
// error messages are never forgotten. 'out T' lets it carry any data type.
sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}