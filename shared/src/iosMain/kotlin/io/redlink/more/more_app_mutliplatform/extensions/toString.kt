package io.redlink.more.more_app_mutliplatform.extensions

import platform.Foundation.*

actual fun Any.asString(): String? {
    return try {
        NSJSONSerialization.dataWithJSONObject(this, NSJSONWritingPrettyPrinted, null)?.let {
           return NSString.create(it, NSUTF8StringEncoding) as String?
        }
    } catch (e: Exception) {
        println(e)
        null
    }
}