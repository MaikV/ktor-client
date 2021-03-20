package com.dorcaapps.android.ktorclient.extensions

import io.ktor.client.features.ClientRequestException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText

suspend fun HttpResponse.throwOnError() =
    if (status.value in 200..299) this
    else throw ClientRequestException(call.response, call.response.readText())