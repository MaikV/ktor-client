@file:UseSerializers(serializerClasses = [ContentTypeSerializer::class])

package com.dorcaapps.android.ktorclient.model.shared

import com.dorcaapps.android.ktorclient.model.ContentTypeSerializer
import io.ktor.http.ContentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class MediaData(
    val id: Int,
    val contentType: ContentType
)