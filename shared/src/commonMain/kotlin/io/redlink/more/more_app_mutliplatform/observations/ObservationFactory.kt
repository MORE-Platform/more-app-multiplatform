package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.log
import io.realm.kotlin.internal.platform.freeze
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.more_app_mutliplatform.observations.simpleQuestionObservation.SimpleQuestionObservation
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.flow.firstOrNull


abstract class ObservationFactory(private val dataManager: ObservationDataManager) {
    val observations = mutableSetOf<Observation>()

    private val studyObservationTypes = mutableSetOf<String>()

    init {
        observations.add(SimpleQuestionObservation())
        observations.add(LimeSurveyObservation())
        Scope.launch {
            ObservationRepository().observationTypes().firstOrNull()?.let {
                studyObservationTypes += it
            }
        }
    }

    fun addNeededObservationTypes(observationTypes: Set<String>) {
        studyObservationTypes += observationTypes
    }

    fun clearNeededObservationTypes() {
        studyObservationTypes.clear()
    }

    fun observationTypes() = observations.map { it.observationType.observationType }.toSet()

    fun sensorPermissions() =
        observations.map { it.observationType.sensorPermissions }.flatten().toSet()

    fun bleDevicesNeeded(): Set<String> {
        log { "Getting BLE devices for types: $studyObservationTypes" }
        return observations.filter { it.observationType.observationType in studyObservationTypes }
            .flatMap { it.bleDevicesNeeded() }.toSet()
    }

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