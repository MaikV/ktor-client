package com.dorcaapps.android.ktorclient.model

import io.ktor.http.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class ContentTypeSerializer : KSerializer<ContentType> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("ContentTypeSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): ContentType = ContentType.parse(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: ContentType) {
        encoder.encodeString(value.toString())
    }
}