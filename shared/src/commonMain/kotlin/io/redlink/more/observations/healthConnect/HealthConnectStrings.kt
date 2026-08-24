package io.redlink.more.observations.healthConnect

import dev.icerock.moko.resources.StringResource

/**
 * Platform-specific wording for the Health Connect provider (e.g. "Google Health Connect" on
 * Android vs. "Apple Health" on iOS) and its subtypes.
 *
 * Adding a new Health Connect subtype only requires adding a new string resource here (plus the
 * matching HealthConnectDataType/HealthConnectSample/HealthConnectCollector).
 */
expect object HealthConnectStrings {
    val providerTypeString: StringResource
    val providerShortTypeString: StringResource
    val heartRateTypeString: StringResource
    val stepsTypeString: StringResource
}
