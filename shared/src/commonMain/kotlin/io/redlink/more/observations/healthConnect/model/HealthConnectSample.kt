package io.redlink.more.observations.healthConnect.model

import io.redlink.more.observations.healthConnect.HealthConnectDataType
import kotlin.time.Instant

/**
 * Health transformation model: the typed sample a
 * [io.redlink.more.observations.healthConnect.HealthConnectCollector] produces from the platform
 * SDK, transformed into the generic payload consumed by `Observation.storeData`. The payload shape
 * mirrors `openapi/HealthTransformationAPI.yaml`'s `TimeData`/`StepData`/`HeartRateData` models
 * (`timestamp`/`startTime`/`endTime`/`device`/`data`/`additionalData`) - those generated classes
 * aren't compiled in because their `oneOf` union collapses into a single class with mutually
 * exclusive fields marked `@Required`, so the shape is reproduced here as a plain map instead.
 *
 * Adding a new subtype means adding a new case here plus a `transform()` branch.
 */
sealed class HealthConnectSample(
    val timestamp: Instant,
    /** Recording hardware (e.g. "Apple Watch"), when the provider exposes it. */
    val device: String? = null,
    /** App that wrote the sample (e.g. "com.apple.health"), surfaced via `additionalData`. */
    val sourceApp: String? = null,
) {
    class HeartRate(
        timestamp: Instant,
        val bpm: Int,
        device: String? = null,
        sourceApp: String? = null,
    ) : HealthConnectSample(timestamp, device, sourceApp)

    class Steps(
        timestamp: Instant,
        val count: Long,
        val start: Instant,
        val end: Instant,
        device: String? = null,
        sourceApp: String? = null,
        /** Only set on the daily-aggregate sample built by `HealthConnectObservation`. */
        val stepsGoal: Int? = null,
        /** Only set on the daily-aggregate sample built by `HealthConnectObservation`. */
        val distanceInMeters: Double? = null,
    ) : HealthConnectSample(timestamp, device, sourceApp)

    fun transform(): Map<String, Any> = buildMap {
        put("timestamp", timestamp.toString())
        device?.let { put("device", it) }
        sourceApp?.let { put("additionalData", mapOf("sourceApp" to it)) }
        when (this@HealthConnectSample) {
            is HeartRate -> put("data", mapOf(HealthConnectDataType.HEART_RATE.valueKey to bpm))
            is Steps -> {
                put("startTime", start.toString())
                put("endTime", end.toString())
                put("data", buildMap {
                    put(HealthConnectDataType.STEPS.valueKey, count)
                    stepsGoal?.let { put("stepsGoal", it) }
                    distanceInMeters?.let { put("distanceInMeters", it) }
                })
            }
        }
    }
}
