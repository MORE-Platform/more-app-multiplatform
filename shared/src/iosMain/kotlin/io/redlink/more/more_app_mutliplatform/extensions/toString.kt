package io.redlink.more.more_app_mutliplatform.extensions

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*

@OptIn(ExperimentalForeignApi::class)
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