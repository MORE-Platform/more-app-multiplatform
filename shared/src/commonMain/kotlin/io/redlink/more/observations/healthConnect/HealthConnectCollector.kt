package io.redlink.more.observations.healthConnect

import io.redlink.more.HEALTH_COLLECTOR_GROUP
import io.redlink.more.observations.BundledPermissionCollector
import io.redlink.more.observations.healthConnect.model.HealthConnectSample
import kotlin.time.Instant

/**
 * Platform integration point for a single Health Connect subtype (e.g. heart rate, steps).
 * Implemented once per subtype per platform (Android: Health Connect Client, iOS: HealthKit) and
 * injected into the single, shared [HealthConnectObservation].
 *
 * Adding a new subtype means implementing this interface once per platform and registering it
 * with [HealthConnectObservation] in the platform `ObservationFactory` - no change to
 * [HealthConnectObservation] itself is required.
 */
interface HealthConnectCollector : BundledPermissionCollector {
    override val permissionGroup: String
        get() = HEALTH_COLLECTOR_GROUP
    val dataType: HealthConnectDataType
    override val permissionKey: String
        get() = dataType.subTypeValue

    suspend fun collect(
        from: Instant,
        to: Instant
    ): List<HealthConnectSample>

    /**
     * Total distance walked/run in the window, for [HealthConnectDataType.aggregatesDaily]
     * subtypes that have an associated distance metric (steps). Not implemented by every
     * collector - unrelated subtypes (e.g. heart rate) keep the `null` default.
     */
    suspend fun collectDistanceInMeters(from: Instant, to: Instant): Double? = null

    /**
     * True when a bonus permission (e.g. steps' distance, see [collectDistanceInMeters]) has
     * never been requested, even though [permissionState] already reports
     * [io.redlink.more.services.store.PermissionApprovalState.GRANTED] for the primary metric -
     * [permissionState] deliberately ignores bonus fields so a denial there can't block the
     * primary metric, which means it's also the only signal
     * [HealthConnectObservation.checkRequiredCollectorPermissions] has that a request is still
     * owed once the primary metric is already settled. Not implemented by every collector -
     * subtypes without a bonus field (e.g. heart rate) keep the `false` default.
     */
    suspend fun hasUnrequestedBonusPermission(): Boolean = false
}