/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license with Commons Clause
 * (see https://www.apache.org/licenses/LICENSE-2.0 and
 * https://commonsclause.com/).
 */

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