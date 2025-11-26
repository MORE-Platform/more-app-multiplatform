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
package io.redlink.umm.participant.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

fun CoroutineScope.repeatEveryFewSeconds(
    intervalMillis: Long,
    initialDelay: Long = 0,
    coroutineContext: CoroutineContext = Dispatchers.Default,
    action: suspend CoroutineScope.() -> Unit
): Job {
    return launch(coroutineContext) {
        if (initialDelay > 0) {
            delay(initialDelay)
        }
        while (isActive) {
            action()
            delay(intervalMillis)
        }
    }
}