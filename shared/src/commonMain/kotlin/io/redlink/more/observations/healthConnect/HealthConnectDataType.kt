package io.redlink.more.observations.healthConnect

import dev.icerock.moko.resources.StringResource
import io.redlink.more.HEALTH_CONNECT_PREFIX
import io.redlink.more.SharedRes

/**
 * One entry per Health Connect subtype. Adding a new subtype means:
 * 1. one new entry here, with the exact subtype value string also registered in
 *    `HEALTH_CONNECT.subTypes` in [io.redlink.more.observations.observationTypes.ObservationType],
 *    plus the [valueKey] under which [HealthConnectValueFormatter][io.redlink.more.formatter.HealthConnectValueFormatter]
 *    reads the stored value (at the root of the sample payload) and the [label]/[unit] shown
 *    in the current-value display,
 * 2. one new [io.redlink.more.observations.healthConnect.model.HealthConnectSample] case,
 * 3. one new [HealthConnectCollector] implementation per platform,
 * 4. [aggregatesDaily] = true if the raw provider samples are per-interval rather than a running
 *    total (e.g. steps) - [io.redlink.more.observations.healthConnect.HealthConnectObservation]
 *    then sums a full calendar day's samples into one stored data point instead of storing each
 *    interval separately.
 *
 * ponytail: valueKey/label/unit/aggregatesDaily are constant per subtype (no per-observation JSON
 * config), so they live on the enum instead of a resolver. If a subtype ever needs config-driven
 * target/fallback values, promote these into a `HealthConnectDisplayDefinition`/
 * `HealthConnectDisplayResolver` pair mirroring [io.redlink.more.resolver.ExternalGoalDisplayResolver].
 */
enum class HealthConnectDataType(
    val subTypeValue: String,
    val valueKey: String,
    val label: StringResource,
    val unit: StringResource,
    val aggregatesDaily: Boolean = false,
) {
    HEART_RATE(
        subTypeValue = "$HEALTH_CONNECT_PREFIX-heart-rate-observation",
        valueKey = "hr",
        label = SharedRes.strings.external_goal_heart_rate_label,
        unit = SharedRes.strings.external_goal_heart_rate_unit,
    ),
    STEPS(
        subTypeValue = "$HEALTH_CONNECT_PREFIX-steps-observation",
        valueKey = "steps",
        label = SharedRes.strings.external_goal_steps_label,
        unit = SharedRes.strings.health_steps_unit,
        aggregatesDaily = true,
    );

    companion object {
        fun fromObservationType(type: String) = entries.firstOrNull { it.subTypeValue == type }
    }
}
