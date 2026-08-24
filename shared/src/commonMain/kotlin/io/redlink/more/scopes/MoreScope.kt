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
package io.redlink.more.scopes

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

interface MoreScope : CoroutineScope {
    fun launch(
        coroutineContext: CoroutineContext = AppDispatchers.default,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job>

    fun repeatedLaunch(
        intervalMillis: Long,
        coroutineContext: CoroutineContext = AppDispatchers.default,
        initalDelay: Long = 0,
        block: suspend CoroutineScope.() -> Unit
    ): Pair<String, Job>

    fun cancel(uuid: String)
    fun cancel(uuids: Collection<String>)
    fun cancel()
}