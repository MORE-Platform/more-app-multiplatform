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
        EventCollection.logEvent(event, message)
    }
}

fun LogEvent.track(data: Map<String, Any> = emptyMap()) {
    val message = if (data.isEmpty()) null else data.entries.joinToString(",") { "${it.key}=${it.value}" }
    EventCollection.logEvent(this, message)
}

@Deprecated("Use LogEvent.track() instead", ReplaceWith("event.track()"))
fun Napier.event(event: LogEvent, message: String? = null) {
    KMMLogger.event(event, message)
}