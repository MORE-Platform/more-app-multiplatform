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
package io.redlink.umm.participant.observations

import io.github.aakira.napier.Napier
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.observations.garmin.GarminObservation
import io.redlink.umm.participant.observations.limesurvey.LimeSurveyObservation
import io.redlink.umm.participant.observations.simpleQuestionObservation.SimpleQuestionObservation
import io.redlink.umm.participant.scopes.Scope
import io.redlink.umm.participant.services.notification.NotificationManager
import io.redlink.umm.participant.services.store.CredentialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

abstract class ObservationFactory(
    repository: MainRepository,
    private val dataManager: ObservationDataManager
) {
    private var credentialRepository: CredentialRepository? = null
    val observations = mutableSetOf<Observation>()

    private val _studyObservationTypes: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    val studyObservationTypes: StateFlow<Set<String>> = _studyObservationTypes

    init {
        observations.add(SimpleQuestionObservation(repository))
        observations.add(LimeSurveyObservation(repository))
        observations.add(GarminObservation(repository))
        Scope.launch(Dispatchers.IO) {
            repository.observation.observationTypes().collect {
                Napier.i(tag = "ObservationFactory::init") { "Observation types fetched: $it" }
                _studyObservationTypes.value = it
            }
        }
    }

    fun addNeededObservationTypes(observationTypes: Set<String>) {
        Napier.i(tag = "ObservationFactory::addNeededObservationTypes") { "Adding observation types to studyObservationTypes: $observationTypes" }
        _studyObservationTypes.value += observationTypes
    }

    fun clearNeededObservationTypes() {
        _studyObservationTypes.value = setOf()
        ObservationStates.resetAll()
    }

    fun setCredentialsRepository(credentialRepository: CredentialRepository) {
        this.credentialRepository = credentialRepository
    }

    fun studySensorPermissions() =
        observations.filter { it.observationType.observationType in studyObservationTypes.value }
            .map { it.observationType.sensorPermissions }.flatten().toSet()

    fun setNotificationManager(notificationManager: NotificationManager) {
        observations.forEach { it.setNotificationManager(notificationManager) }
    }

    fun observationTypes() = observations.map { it.observationType.observationType }.toSet()

    fun sensorPermissions() =
        observations.map { it.observationType.sensorPermissions }.flatten().toSet()

    fun bleDevicesNeeded(): Set<String> {
        Napier.i(tag = "ObservationFactory::bleDevicesNeeded") { "Filtering types for BLE: ${studyObservationTypes.value}" }
        val bleTypes =
            observations.filter { it.observationType.observationType in studyObservationTypes.value }
                .flatMap { it.bleDevicesNeeded() }.toSet()
        Napier.i(tag = "ObservationFactory::bleDevicesNeeded") { "BLE observation types: $bleTypes" }
        return bleTypes
    }

    fun autoStartableObservations(): Set<String> {
        val autoStartTypes = studyObservations().filter { it.ableToAutomaticallyStart() }
            .map { it.observationType.observationType }.toSet()
        Napier.i(tag = "ObservationFactory::autoStartableObservations") { "Auto-startable observations: $autoStartTypes" }
        return autoStartTypes
    }

    suspend fun updateObservationErrors() {
        if (this.credentialRepository?.hasCredentials?.value == true) {
            studyObservations().forEach { it.updateObservationErrors() }
        }
    }

    fun observation(type: String): Observation? {
        Napier.i(tag = "ObservationFactory::observation") { "Fetching observation of type: $type" }
        return observations.firstOrNull { it.observationType.observationType == type }?.apply {
            if (!this.observationDataManagerAdded()) {
                Napier.i(tag = "ObservationFactory::observation") { "Adding data manager to observation of type: $type" }
                setDataManager(dataManager)
            }
        }
    }

    private fun studyObservations() =
        observations.filter { it.observationType.observationType in studyObservationTypes.value }

}