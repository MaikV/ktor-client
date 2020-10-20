package com.dorcaapps.android.ktorclient.model

sealed class Resource<out T> {
    data class Success<T>(val data: T): Resource<T>()
    object Loading: Resource<Nothing>()
    data class Error(val throwable: Throwable): Resource<Nothing>()
}