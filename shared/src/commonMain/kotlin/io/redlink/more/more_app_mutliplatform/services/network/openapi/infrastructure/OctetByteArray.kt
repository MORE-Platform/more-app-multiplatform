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

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@Serializable
class OctetByteArray(val value: ByteArray) {
    @Serializer(OctetByteArray::class)
    companion object : KSerializer<OctetByteArray> {
        override val descriptor = PrimitiveSerialDescriptor("OctetByteArray", PrimitiveKind.STRING)
        override fun serialize(encoder: Encoder, obj: OctetByteArray) = encoder.encodeString(hex(obj.value))
        override fun deserialize(decoder: Decoder) = OctetByteArray(hex(decoder.decodeString()))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as OctetByteArray
        return value.contentEquals(other.value)
    }

    override fun hashCode(): Int {
        return value.contentHashCode()
    }

    override fun toString(): String {
        return "OctetByteArray(${hex(value)})"
    }
}