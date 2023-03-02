package io.redlink.more.more_app_mutliplatform.observations

import io.realm.kotlin.types.RealmInstant
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema

open class Observation(private val observationIdentification: String, private val dataManager: ObservationDataManager) {

    open fun storeData(observationDataSchema: ObservationDataSchema) {
        dataManager.add(observationDataSchema.apply {
            this.observationId = observationIdentification
            if (this.timestamp == null) {
                this.timestamp = RealmInstant.now()
            }
        })
    }

    fun storeAndSend(observationDataSchema: ObservationDataSchema) {
        storeData(observationDataSchema)
        dataManager.saveAndSend()
    }
}