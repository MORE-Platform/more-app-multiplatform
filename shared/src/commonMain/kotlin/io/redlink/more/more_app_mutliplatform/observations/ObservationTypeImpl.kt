package io.redlink.more.more_app_mutliplatform.observations

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema

class ObservationTypeImpl(val observationType: String, val sensorPermissions: Set<String>) {
    fun addObservationType(schema: ObservationDataSchema): ObservationDataSchema {
        val obsType = observationType
        return schema.apply { this.observationType = obsType}
    }
}