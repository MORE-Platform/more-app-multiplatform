package io.redlink.more.observations.accelerometer

import io.github.aakira.napier.Napier
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.events.TimedEventBus
import io.redlink.more.observations.Observation
import io.redlink.more.observations.accelerometer.BackgroundAccelerometerObservation.Companion.DEFAULT_RECORD_DURATION_SECONDS
import io.redlink.more.observations.accelerometer.BackgroundAccelerometerObservation.Companion.MAX_RECORD_DURATION_SECONDS
import io.redlink.more.observations.observationTypes.AccelerometerType
import io.redlink.more.observations.observers.ManualObserver
import io.redlink.more.scopes.Scope
import io.redlink.more.services.store.PermissionApprovalState
import io.redlink.more.viewModels.ViewManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/**
 * Shared observation for OS-level background accelerometer recording (iOS `CMSensorRecorder`,
 * behind [BackgroundAccelerometerCollector]). Recording itself happens on-device independent of
 * this observation's lifecycle; this class arms/re-arms the recorder for the active task window
 * and periodically drains recorded samples into the DB - on app foregrounding, on
 * [collectAllData] (background poll run, see [io.redlink.more.observations.polling.PollingTaskScheduler]),
 * and on [stop]/[store].
 */
class BackgroundAccelerometerObservation(
    repos: MainRepository,
    sensorPermissions: Set<String>,
    private val collector: BackgroundAccelerometerCollector
) : Observation(repos, AccelerometerType(sensorPermissions)), ManualObserver {
    private var taskStart: Instant? = null
    private var taskStop: Instant? = null
    private var recordDurationSeconds: Double = DEFAULT_RECORD_DURATION_SECONDS

    init {
        Scope.launch(Dispatchers.Default) {
            TimedEventBus.subscribe {
                collectWindow()
            }
        }
        Scope.launch(Dispatchers.Default) {
            ViewManager.appInForeground.collect {
                if (it) {
                    collectAllData()
                }
            }
        }
    }

    override fun start(): Boolean {
        if (observerAccessible()) {
            collector.record(recordDurationSeconds)
            Scope.launch(Dispatchers.IO) {
                delay(5.seconds)
                withContext(Dispatchers.Main) {
                    collectWindow()
                }
            }
            return true
        }
        return false
    }

    override fun stop(onCompletion: () -> Unit) {
        Scope.launch(Dispatchers.Default) {
            collectWindow()
            onCompletion()
        }
    }

    override fun store(start: Long, end: Long, onCompletion: () -> Unit) {
        Scope.launch(Dispatchers.Default) {
            collectWindow()
            super.store(start, end, onCompletion)
        }
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {
        val start = (settings[CONFIG_TASK_START] as? Long)?.let(Instant::fromEpochSeconds)
        val stop = (settings[CONFIG_TASK_STOP] as? Long)?.let(Instant::fromEpochSeconds)
        taskStart = start
        taskStop = stop
        recordDurationSeconds = computeRecordDuration(start, stop, Clock.System.now())
    }

    /**
     * Entry point for a background poll run - see [ObservationFactory.pollActiveObservations][io.redlink.more.observations.ObservationFactory.pollActiveObservations].
     * Re-arms the recorder for the remaining task window so recording continues past the
     * original window while the app stays in the background.
     */
    override suspend fun collectAllData() {
        registerRecentSchedules()
        collectWindow()
        computeReArmDuration(taskStop, Clock.System.now())?.let { collector.record(it) }
    }

    override fun observerErrors(): Set<String> {
        val errors = mutableSetOf<String>()
        if (!collector.isRecordingAvailable) {
            errors.add("Accelerometer Recording is not available")
        }
        if (hasPermission() != PermissionApprovalState.GRANTED) {
            errors.add("Permission not granted to access Sensor recording service")
        }
        return errors
    }

    override fun pollIntervalMillis(): Long = POLL_INTERVAL_MILLIS

    private suspend fun collectWindow() {
        val lastCollected = getLastCollectionTimestamp() ?: return
        val (from, to) = computeWindow(lastCollected, taskStart, taskStop, Clock.System.now())
            ?: return
        val data = collector.collect(from, to)
        Napier.d { "New Acc data: $data" }
        if (data.isNotEmpty()) {
            storeData(data) {}
            collectionTimestampToNow()
        }
        Napier.d(tag = "BackgroundAccelerometerObservation") { "Collected ${data.size} accelerometer samples from $from to $to" }
    }

    companion object {
        private const val POLL_INTERVAL_MILLIS = 15 * 60 * 1000L
        private const val DEFAULT_RECORD_DURATION_SECONDS = 60.0 * 10
        private const val MAX_RECORD_DURATION_SECONDS = 60.0 * 60 * 12

        /**
         * Seconds to arm [BackgroundAccelerometerCollector.record] for, derived from the task
         * window - falls back to [DEFAULT_RECORD_DURATION_SECONDS] when there is no usable window
         * (missing bounds, or already elapsed), and clamps to [MAX_RECORD_DURATION_SECONDS] (the
         * `CMSensorRecorder` ceiling).
         */
        internal fun computeRecordDuration(
            taskStart: Instant?,
            taskStop: Instant?,
            now: Instant
        ): Double {
            if (taskStart == null || taskStop == null || taskStop <= now) {
                return DEFAULT_RECORD_DURATION_SECONDS
            }
            val effectiveStart = if (taskStart < now) now else taskStart
            return (taskStop - effectiveStart).inWholeSeconds.toDouble()
                .coerceAtMost(MAX_RECORD_DURATION_SECONDS)
        }

        /**
         * Seconds remaining in the task window to re-arm [BackgroundAccelerometerCollector.record]
         * for on a background poll run, or `null` when the window is already over (nothing to
         * re-arm for).
         */
        internal fun computeReArmDuration(taskStop: Instant?, now: Instant): Double? {
            if (taskStop == null || taskStop <= now) return null
            return (taskStop - now).inWholeSeconds.toDouble()
                .coerceAtMost(MAX_RECORD_DURATION_SECONDS)
        }
    }
}
