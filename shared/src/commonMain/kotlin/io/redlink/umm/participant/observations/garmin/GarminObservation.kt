package io.redlink.umm.participant.observations.garmin

import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.observations.Observation
import io.redlink.umm.participant.observations.observationTypes.GarminType

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