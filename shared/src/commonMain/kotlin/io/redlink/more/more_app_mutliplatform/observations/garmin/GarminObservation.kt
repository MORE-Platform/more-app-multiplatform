package io.redlink.more.more_app_mutliplatform.observations.garmin

import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.observations.Observation
import io.redlink.more.more_app_mutliplatform.observations.observationTypes.GarminType

class GarminObservation(repos: MainRepository) :
    Observation(repos, GarminType()) {
    override fun start(): Boolean {
        return true
    }

    override fun stop(onCompletion: () -> Unit) {
        onCompletion()
    }

    override fun applyObservationConfig(settings: Map<String, Any>) {

    }
}