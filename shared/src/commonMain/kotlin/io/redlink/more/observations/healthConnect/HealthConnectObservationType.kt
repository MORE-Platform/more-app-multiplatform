package io.redlink.more.observations.healthConnect

import io.redlink.more.HEALTH_CONNECT_PREFIX
import io.redlink.more.observations.observationTypes.ObservationType

class HealthConnectObservationType : ObservationType(
    observationType = "$HEALTH_CONNECT_PREFIX-observation",
    sensorPermissions = emptySet(),
    prefix = "$HEALTH_CONNECT_PREFIX-"
)
