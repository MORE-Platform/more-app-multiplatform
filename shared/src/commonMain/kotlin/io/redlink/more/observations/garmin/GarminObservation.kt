package io.redlink.more.observations.garmin

import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.Observation
import io.redlink.more.observations.observationTypes.GarminType

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