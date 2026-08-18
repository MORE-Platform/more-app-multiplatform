package io.redlink.more.observations.accelerometer

import io.redlink.more.observations.Collector
import io.redlink.more.observations.ObservationBulkModel
import kotlin.time.Instant

/**
 * Platform integration point for OS-level background accelerometer recording (iOS:
 * `CMSensorRecorder`, which keeps buffering samples on-device while the app is suspended or
 * killed; Android has no equivalent, so no Android implementation is expected).
 */
interface BackgroundAccelerometerCollector : Collector {
    /** False when the OS cannot record accelerometer data in the background on this device. */
    val isRecordingAvailable: Boolean

    /** Arms the OS-side recorder for the next [durationSeconds]; samples are read back via [collect]. */
    fun record(durationSeconds: Double)

    suspend fun collect(from: Instant, to: Instant): List<ObservationBulkModel>
}
