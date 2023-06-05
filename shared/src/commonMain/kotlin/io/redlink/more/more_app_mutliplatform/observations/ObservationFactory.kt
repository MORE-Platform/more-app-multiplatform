package io.redlink.more.more_app_mutliplatform.observations

import io.realm.kotlin.internal.platform.freeze
import io.redlink.more.more_app_mutliplatform.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.more_app_mutliplatform.observations.simpleQuestionObservation.SimpleQuestionObservation


abstract class ObservationFactory(private val dataManager: ObservationDataManager) {
    val observations = mutableSetOf<Observation>()

    init {
        observations.add(SimpleQuestionObservation())
        observations.add(LimeSurveyObservation())
    }

    fun observationTypes() = observations.map { it.observationType.observationType }.toSet()

    fun sensorPermissions() =
        observations.map { it.observationType.sensorPermissions }.flatten().toSet()

    fun bleDevicesNeeded(types: Set<String>) =
        observations.filter { it.observationType.observationType in types }
            .flatMap { it.bleDevicesNeeded() }.toSet()

    fun autoStartableObservations() = observations.filter { it.ableToAutomaticallyStart() }
        .map { it.observationType.observationType }.toSet()

    fun observation(type: String): Observation? {
        return observations.firstOrNull { it.observationType.observationType == type }?.apply {
            if (!this.observationDataManagerAdded()) {
                setDataManager(dataManager)
            }
        }?.freeze()
    }
}