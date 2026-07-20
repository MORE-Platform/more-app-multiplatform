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

package io.redlink.more.logging

import io.github.aakira.napier.Napier
import io.redlink.more.observations.appUsage.model.LogEvent

object KMMLogger {
    const val EVENT_TAG = "EVENT"
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

    fun event(event: LogEvent, message: String? = null) {
        val logMessage = "[EVENT: ${event.key}] ${message ?: ""}".trim()
        Napier.i(logMessage, tag = EVENT_TAG)
        EventCollection.logEvent(event, message)
    }
}

fun Napier.event(event: LogEvent, message: String? = null) {
    KMMLogger.event(event, message)
}