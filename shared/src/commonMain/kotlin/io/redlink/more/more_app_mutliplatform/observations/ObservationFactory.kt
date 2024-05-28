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
package io.redlink.more.more_app_mutliplatform.observations

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
import io.redlink.more.more_app_mutliplatform.extensions.appendAll
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.clear
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.more_app_mutliplatform.observations.simpleQuestionObservation.SimpleQuestionObservation
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update


abstract class ObservationFactory(private val dataManager: ObservationDataManager) {
    val observations = mutableSetOf<Observation>()

    private val _studyObservationTypes: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    val studyObservationTypes: StateFlow<Set<String>> = _studyObservationTypes
    private val _observationErrors: MutableStateFlow<Map<String, Set<String>>> =
        MutableStateFlow(emptyMap())
    val observationErrors: StateFlow<Map<String, Set<String>>> = _observationErrors

    private var observationErrorWatcher: Job? = null

    init {
        observations.add(SimpleQuestionObservation())
        observations.add(LimeSurveyObservation())
        Scope.launch(Dispatchers.IO) {
            ObservationRepository().observationTypes().collect {
                Napier.i(tag = "ObservationFactory::init") { "Observation types fetched: $it" }
                _studyObservationTypes.clear()
                _studyObservationTypes.appendAll(it)
            }
        }
        Scope.launch(Dispatchers.IO) {
            studyObservationTypes.collect {
                Napier.d(tag = "ObservationFactory::init::StudyObservationTypes") { it.toString() }
                if (it.isNotEmpty()) {
                    Napier.d { "Study types not empty" }
                    listenToObservationErrors()
                } else {
                    Napier.d { "Study types empty" }
                    observationErrorWatcher?.cancel()
                    observationErrorWatcher = null
                    _observationErrors.update { emptyMap() }
                }
            }
        }
    }

    fun addNeededObservationTypes(observationTypes: Set<String>) {
        Napier.i(tag = "ObservationFactory::addNeededObservationTypes") { "Adding observation types to studyObservationTypes: $observationTypes" }
        _studyObservationTypes.appendAll(observationTypes)
    }

    fun clearNeededObservationTypes() {
        _studyObservationTypes.clear()
        observationErrorWatcher?.cancel()
        observationErrorWatcher = null
        _observationErrors.update { emptyMap() }
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

    private fun listenToObservationErrors() {
        val flowList = studyObservations().map { it.observationErrors }
        val combinedFlow = combine(flowList) { values ->
            values.toMap()
        }
        observationErrorWatcher?.cancel()
        observationErrorWatcher = Scope.launch {
            Napier.d(tag = "ObservationFactory::listenToObservationErrors") { "Listening for observation errors" }
            combinedFlow.cancellable().collect {
                _observationErrors.set(it)
                Napier.d(tag = "ObservationFactory::updateObservationErrors") { observationErrors.value.toString() }
            }
        }.second
    }

    fun updateObservationErrors() {
        studyObservations().forEach { it.updateObservationErrors() }
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

    fun observationErrorsAsClosure(state: (Map<String, Set<String>>) -> Unit) =
        observationErrors.asClosure(state)
}