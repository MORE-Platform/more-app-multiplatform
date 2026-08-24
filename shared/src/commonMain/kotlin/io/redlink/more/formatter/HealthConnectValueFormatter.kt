package io.redlink.more.formatter

import dev.icerock.moko.resources.desc.desc
import io.redlink.more.extensions.jsonRead
import io.redlink.more.extensions.localDate
import io.redlink.more.models.DataDisplayValue
import io.redlink.more.observations.healthConnect.HealthConnectDataType
import kotlin.time.Instant

/**
 * Formats the raw JSON payload stored by [io.redlink.more.observations.healthConnect.HealthConnectObservation]
 * (e.g. `{"timestamp":...,"hr":72}`, `{"timestamp":...,"steps":1000,"stepsGoal":10000}`) into a
 * [DataDisplayValue], keyed by [HealthConnectDataType].
 */
class HealthConnectValueFormatter {
    companion object {
        fun format(observationType: String, currentValue: Any?): DataDisplayValue? {
            val type = HealthConnectDataType.fromObservationType(observationType) ?: return null
            val payload = (currentValue as? String)?.jsonRead<Map<String, Any?>>() ?: return null
            val data = payload["data"] as? Map<String, Any?> ?: return null
            val timestamp = (payload["timestamp"] as? String)
                ?.let { runCatching { Instant.parse(it).localDate() }.getOrNull() }
            return when (type) {
                HealthConnectDataType.STEPS -> {
                    val steps = data[type.valueKey]?.toString() ?: "-"
                    val goal = data["stepsGoal"]?.toString()
                    DataDisplayValue(
                        value = goal?.let { "$steps / $it" } ?: steps,
                        unit = type.unit.desc(),
                        label = type.label.desc(),
                        timestamp = timestamp
                    )
                }
                // composite subtypes (e.g. blood pressure, sleep) get their own branch here
                else -> DataDisplayValue(
                    value = data[type.valueKey]?.toString() ?: "-",
                    unit = type.unit.desc(),
                    label = type.label.desc(),
                    timestamp = timestamp
                )
            }
        }
    }
}
