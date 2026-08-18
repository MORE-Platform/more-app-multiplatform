package io.redlink.more.extensions

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

@PublishedApi
internal val sharedJson: Json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

inline fun <reified T> String.jsonRead(): T? =
    runCatching { sharedJson.decodeFromString<T>(this) }
        .recoverCatching { sharedJson.parseToJsonElement(this).toAny() as T }
        .getOrNull()

inline fun <reified T> T.jsonString(): String =
    runCatching { sharedJson.encodeToString(this) }.getOrDefault("{}")

fun JsonElement.toAny(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> {
        if (isString) {
            content
        } else {
            booleanOrNull ?: longOrNull ?: doubleOrNull ?: content
        }
    }

    is JsonObject -> mapValues { it.value.toAny() }
    is JsonArray -> map { it.toAny() }
}
