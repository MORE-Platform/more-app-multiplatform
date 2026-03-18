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

import io.redlink.more.observations.appUsage.model.LogEvent

interface EventObserver {
    fun onEvent(event: LogEvent, message: String?)
}

private data class LogEventQueueItem(val event: LogEvent, val message: String?)

object EventCollection {
    private val observers = mutableListOf<EventObserver>()

    private val eventQueue = mutableListOf<LogEventQueueItem>()

    fun addObserver(observer: EventObserver) {
        if (!observers.contains(observer)) {
            observers.add(observer)
            eventQueue.forEach {
                logEvent(it.event, it.message)
            }
            eventQueue.clear()
        }
    }

    fun removeObserver(observer: EventObserver) {
        observers.remove(observer)
    }

    fun logEvent(event: LogEvent, message: String? = null) {
        if (observers.isEmpty()) {
            eventQueue.add(LogEventQueueItem(event, message))
        } else {
            observers.forEach { it.onEvent(event, message) }
        }
    }

    fun clearQueue() {
        eventQueue.clear()
    }
}
