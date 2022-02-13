package com.dorcaapps.android.ktorclient.model

import androidx.annotation.IntRange

sealed class Resource<out T>(open val data: T?) {
    data class Success<T>(override val data: T) : Resource<T>(data = data)
    data class Loading<T>(
        @IntRange(from = 0, to = 100) val progressPercent: Int,
        override val data: T? = null
    ) : Resource<T>(data = data)

    data class Failure<T>(val throwable: Throwable, override val data: T? = null) :
        Resource<T>(data = data)
}