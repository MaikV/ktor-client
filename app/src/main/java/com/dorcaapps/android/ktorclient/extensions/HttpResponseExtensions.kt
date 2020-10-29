package com.dorcaapps.android.ktorclient.extensions

import io.ktor.client.features.*
import io.ktor.client.statement.*

fun HttpResponse.throwOnError() =
    if (status.value in 200..299) this
    else throw ClientRequestException(call.response)