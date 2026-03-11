package io.redlink.more.util

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock

/**
 * Emits "now" every [periodMs], aligned so the first tick happens exactly on the next boundary.
 *
 * Example:
 *  - periodMs=1_000  -> next full second
 *  - periodMs=60_000 -> next full minute
 */
fun alignedNowFlow(periodMs: Long = 1000L): Flow<Long> = flow {
    require(periodMs > 0) { "periodMs must be > 0" }

    emit(Clock.System.now().epochSeconds)
    val nowMs = Clock.System.now().toEpochMilliseconds()
    val initialDelay = ((periodMs - (nowMs % periodMs)) % periodMs)

    if (initialDelay != 0L) delay(initialDelay)

    while (currentCoroutineContext().isActive) {
        emit(Clock.System.now().epochSeconds)
        delay(periodMs)
    }
}