package io.redlink.more.more_app_mutliplatform.extensions

import platform.Foundation.NSJSONSerialization
import platform.Foundation.NSJSONWritingPrettyPrinted

actual fun Any.asString(): String? {
    return try {
        val data = NSJSONSerialization.dataWithJSONObject(this, NSJSONWritingPrettyPrinted, null)
        data.toString()
    } catch (e: Exception) {
        println(e)
        null
    }
}