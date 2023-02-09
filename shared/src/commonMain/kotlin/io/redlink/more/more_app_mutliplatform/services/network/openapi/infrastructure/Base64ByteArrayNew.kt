package io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure

import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

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