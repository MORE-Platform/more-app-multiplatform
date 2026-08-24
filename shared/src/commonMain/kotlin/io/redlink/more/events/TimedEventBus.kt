package io.redlink.more.events

import io.github.aakira.napier.Napier
import io.redlink.more.util.alignedNowFlow
import io.redlink.more.util.createUUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object TimedEventBus {
    private val tickDuration = 1.minutes

    private data class Subscription(
        val interval: Duration,
        var lastBucket: Long,
        val onTick: suspend () -> Unit
    )

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val mutex = Mutex()

    private val subscriptions =
        mutableMapOf<String, Subscription>()

    private var tickerJob: Job? = null

    fun subscribe(
        interval: Duration = 1.minutes,
        onTick: suspend () -> Unit
    ): String {
        require(interval.isPositive())

        val id = createUUID()

        scope.launch {
            mutex.withLock {
                subscriptions[id] = Subscription(
                    interval = interval,
                    lastBucket = currentBucket(interval),
                    onTick = onTick
                )
            }

            ensureTickerStarted()
        }

        return id
    }

    fun unsubscribe(id: String) {
        scope.launch {
            val shouldStopTicker = mutex.withLock {
                subscriptions.remove(id)
                subscriptions.isEmpty()
            }

            if (shouldStopTicker) {
                tickerJob?.cancel()
                tickerJob = null
            }
        }
    }

    private fun currentTimestamp(): Long {
        return Clock.System.now()
            .toEpochMilliseconds()
    }

    private fun currentBucket(
        interval: Duration
    ): Long {
        return currentTimestamp() /
                interval.inWholeMilliseconds
    }

    private fun ensureTickerStarted() {
        if (tickerJob?.isActive == true) {
            return
        }

        tickerJob = scope.launch {
            alignedNowFlow(
                periodMs = tickDuration.inWholeMilliseconds
            ).collect {
                tick()
            }
        }
    }

    private suspend fun tick() {
        val now = currentTimestamp()

        val due = mutex.withLock {
            subscriptions.values
                .filter { subscription ->
                    val bucket =
                        now / subscription.interval.inWholeMilliseconds

                    bucket > subscription.lastBucket
                }
                .onEach { subscription ->
                    subscription.lastBucket =
                        now / subscription.interval.inWholeMilliseconds
                }
                .toList()
        }

        due.forEach { subscription ->
            try {
                subscription.onTick()
            } catch (e: Exception) {
                Napier.e(
                    throwable = e,
                    tag = "TimedEventBus"
                ) {
                    "Error notifying subscriber"
                }
            }
        }
    }
}
