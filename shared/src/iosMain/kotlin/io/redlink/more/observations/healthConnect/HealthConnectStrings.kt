package io.redlink.more.observations.healthConnect

import dev.icerock.moko.resources.StringResource
import io.redlink.more.SharedRes

actual object HealthConnectStrings {
    actual val providerTypeString: StringResource = SharedRes.strings.type_health_connect_ios
    actual val providerShortTypeString: StringResource =
        SharedRes.strings.type_health_connect_ios_short
    actual val heartRateTypeString: StringResource = SharedRes.strings.type_health_connect_heart_rate
    actual val stepsTypeString: StringResource = SharedRes.strings.type_health_connect_steps
}
