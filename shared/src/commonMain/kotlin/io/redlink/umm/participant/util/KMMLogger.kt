package io.redlink.umm.participant.util

import io.github.aakira.napier.Napier

// Object for Swift to access the Napier Logger
object KMMLogger {
    fun d(tag: String? = null, message: String) {
        Napier.d(message, tag = tag)
    }

    fun i(tag: String? = null, message: String) {
        Napier.i(message, tag = tag)
    }

    fun w(tag: String? = null, message: String) {
        Napier.w(message, tag = tag)
    }

    fun e(tag: String? = null, message: String) {
        Napier.e(message, tag = tag)
    }
}