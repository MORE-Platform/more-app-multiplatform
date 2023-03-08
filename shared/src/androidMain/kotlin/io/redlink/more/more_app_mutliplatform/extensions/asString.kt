package io.redlink.more.more_app_mutliplatform.extensions

import com.google.gson.Gson

actual fun Any.asString(): String? {
    return Gson().toJson(this)
}