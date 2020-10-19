@file:UseSerializers(serializerClasses = [ContentTypeSerializer::class])

package com.dorcaapps.android.ktorclient.ui.paging

import com.dorcaapps.android.ktorclient.model.ContentTypeSerializer
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
data class MediaData(
    val id: Int,
    val contentType: ContentType
)