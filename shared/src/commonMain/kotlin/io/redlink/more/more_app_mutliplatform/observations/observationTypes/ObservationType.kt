package io.redlink.more.more_app_mutliplatform.observations.observationTypes

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema

open class ObservationType(val observationType: String, val sensorPermissions: Set<String>) {
    fun addObservationType(schema: ObservationDataSchema): ObservationDataSchema {
        val obsType = observationType
        schema.observationType = obsType
        return schema
    }
}