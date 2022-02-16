package com.dorcaapps.android.ktorclient.model.shared

fun Throwable.forceCastException() =
    this as? Exception ?: throw this