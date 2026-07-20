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

package io.redlink.more.observations.appUsage.model

import kotlinx.datetime.Clock

data class EventModel(
    val timestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val eventType: LogEvent,
    val eventData: Map<String, Any> = emptyMap()
)
