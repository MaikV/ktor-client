package com.dorcaapps.android.ktorclient.model

import androidx.annotation.IntRange

sealed class Resource<out T> {
    data class Success<T>(val data: T): Resource<T>()
    data class Loading(@IntRange(from = 0, to = 100) val progressPercent: Int): Resource<Nothing>()
    data class Error(val throwable: Throwable): Resource<Nothing>()
}