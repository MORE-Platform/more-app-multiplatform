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