package io.redlink.more.more_app_mutliplatform.observations

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema

abstract class ObservationType(
    observationId: String,
    private val observationType: String,
    dataManager: ObservationDataManager
) : Observation(observationId, dataManager) {

    override fun storeData(observationDataSchema: ObservationDataSchema) {
        observationType.let {
            super.storeData(observationDataSchema.apply {
                this.observationType = it
            })
        }
    }
}