package io.redlink.more.observations.polling

/**
 * Platform bridge for the single, shared background poll request (a WorkManager `Worker` on
 * Android, a `BGAppRefreshTask` on iOS) that periodically wakes the app to let currently
 * [io.redlink.more.observations.Observation.activate]d polling observations collect data while
 * the app is not in the foreground.
 */
interface PollingTaskScheduler {
    fun schedule(intervalMillis: Long)
    fun cancel()
}
