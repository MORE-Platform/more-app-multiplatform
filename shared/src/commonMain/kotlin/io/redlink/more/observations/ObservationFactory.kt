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
package io.redlink.more.observations

import io.github.aakira.napier.Napier
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.garmin.GarminObservation
import io.redlink.more.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.observations.questionObservation.QuestionObservation
import io.redlink.more.scopes.Scope
import io.redlink.more.services.notification.NotificationManager
import io.redlink.more.services.store.CredentialRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface ObservationFactory {
    val observations: MutableSet<Observation>
    val studyObservationTypes: StateFlow<Set<String>>

    fun addNeededObservationTypes(observationTypes: Set<String>)
    fun clearNeededObservationTypes()
    fun setCredentialsRepository(credentialRepository: CredentialRepository)
    fun studySensorPermissions(): Set<String>
    fun setNotificationManager(notificationManager: NotificationManager)
    fun observationTypes(): Set<String>
    fun getMatchingObservationTypes(types: Set<String>): Set<String>
    fun sensorPermissions(): Set<String>
    fun bleDevicesNeeded(): Set<String>
    fun autoStartableObservations(): Set<String>
    suspend fun updateObservationErrors()
    fun observation(type: String): Observation?
}

abstract class ObservationFactoryImpl(
    repository: MainRepository,
    private val dataManager: ObservationDataManager
) : ObservationFactory {
    private var credentialRepository: CredentialRepository? = null
    override val observations = mutableSetOf<Observation>()

    private val _studyObservationTypes: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    override val studyObservationTypes: StateFlow<Set<String>> = _studyObservationTypes

    init {
        observations.add(QuestionObservation(repository))
        observations.add(LimeSurveyObservation(repository))
        observations.add(GarminObservation(repository))
        Scope.launch(Dispatchers.IO) {
            repository.observation.observationTypes().collect {
                Napier.i(tag = "ObservationFactory::init") { "Observation types fetched: $it" }
                _studyObservationTypes.value = it
            }
        }
    }

    override fun addNeededObservationTypes(observationTypes: Set<String>) {
        Napier.i(tag = "ObservationFactory::addNeededObservationTypes") { "Adding observation types to studyObservationTypes: $observationTypes" }
        _studyObservationTypes.value += observationTypes
    }

    override fun clearNeededObservationTypes() {
        _studyObservationTypes.value = setOf()
        ObservationStates.resetAll()
    }

    override fun setCredentialsRepository(credentialRepository: CredentialRepository) {
        this.credentialRepository = credentialRepository
    }

    override fun studySensorPermissions() =
        observations.filter { observationMatchesStudyTypes(it, studyObservationTypes.value) }
            .flatMap { it.observationType.sensorPermissions }.toSet()

    override fun setNotificationManager(notificationManager: NotificationManager) {
        observations.forEach { it.setNotificationManager(notificationManager) }
    }

    override fun observationTypes() =
        observations.map { it.observationType.observationType }.toSet()

    override fun getMatchingObservationTypes(types: Set<String>): Set<String> =
        observations.filter { it.observationType.matchesAny(types) }
            .map { it.observationType.observationType }.toSet()

    override fun sensorPermissions() =
        observations.map { it.observationType.sensorPermissions }.flatten().toSet()

    override fun bleDevicesNeeded(): Set<String> {
        Napier.i(tag = "ObservationFactory::bleDevicesNeeded") { "Filtering types for BLE: ${studyObservationTypes.value}" }
        val bleTypes =
            observations.filter { observationMatchesStudyTypes(it, studyObservationTypes.value) }
                .flatMap { it.bleDevicesNeeded() }.toSet()
        Napier.i(tag = "ObservationFactory::bleDevicesNeeded") { "BLE observation types: $bleTypes" }
        return bleTypes
    }

    override fun autoStartableObservations(): Set<String> {
        val autoStartTypes = studyObservations().filter { it.ableToAutomaticallyStart() }
            .map { it.observationType.observationType }.toSet()
        Napier.i(tag = "ObservationFactory::autoStartableObservations") { "Auto-startable observations: $autoStartTypes" }
        return autoStartTypes
    }

    override suspend fun updateObservationErrors() {
        if (this.credentialRepository?.hasCredentials?.value == true) {
            studyObservations().forEach { it.updateObservationErrors() }
        }
    }

    override fun observation(type: String): Observation? {
        Napier.i(tag = "ObservationFactory::observation") { "Fetching observation of type: $type" }
        return observations.firstOrNull {
            it.observationType.matches(type)
        }?.apply {
            if (!this.observationDataManagerAdded()) {
                Napier.i(tag = "ObservationFactory::observation") { "Adding data manager to observation of type: $type" }
                setDataManager(dataManager)
            }
        }
    }

    private fun studyObservations(): List<Observation> =
        observations.filter { it.observationType.matchesAny(studyObservationTypes.value) }

    private fun observationMatchesStudyTypes(obs: Observation, types: Set<String>): Boolean =
        obs.observationType.matchesAny(types)

}