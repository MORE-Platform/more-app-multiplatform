/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */
package io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
class Base64ByteArrayNew(val value: ByteArray) {
    @Serializer(Base64ByteArrayNew::class)
    companion object : KSerializer<Base64ByteArrayNew> {
        override val descriptor = PrimitiveSerialDescriptor("Base64ByteArray", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, obj: Base64ByteArrayNew) = encoder.encodeString(obj.value.encodeBase64())
        override fun deserialize(decoder: Decoder) = Base64ByteArrayNew(decoder.decodeString().decodeBase64Bytes())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as Base64ByteArrayNew
        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun toString(): String {
        return "Base64ByteArray(${hex(value)})"
    }
}