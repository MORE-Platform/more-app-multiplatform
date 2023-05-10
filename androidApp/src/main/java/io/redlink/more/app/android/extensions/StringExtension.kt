package io.redlink.more.app.android.extensions

import android.net.Uri

fun String.extractKeyValuePairs(): Map<String, String> {
    val keyValuePairs = mutableMapOf<String, String>()

    val pairs = this.split("&")

    for (pair in pairs) {
        val keyValue = pair.split("=")
        if (keyValue.size == 2) {
            val key = Uri.decode(keyValue[0])
            val value = Uri.decode(keyValue[1])

            keyValuePairs[key] = value
        }
    }

    return keyValuePairs
}