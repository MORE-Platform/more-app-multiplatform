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
import io.redlink.more.more_app_mutliplatform.extensions.append
import io.redlink.more.more_app_mutliplatform.extensions.clear
import io.redlink.more.more_app_mutliplatform.observations.selfLearningMultipleChoiceQuestionObservation.SelfLearningMultipleChoiceQuestionObservation
import io.redlink.more.more_app_mutliplatform.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.more_app_mutliplatform.observations.simpleQuestionObservation.SimpleQuestionObservation
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.NotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull


abstract class ObservationFactory(private val dataManager: ObservationDataManager) {
    val observations = mutableSetOf<Observation>()

    val studyObservationTypes: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())

    init {
        observations.add(SimpleQuestionObservation())
        observations.add(LimeSurveyObservation())
        observations.add(SelfLearningMultipleChoiceQuestionObservation())
        Scope.launch {
            ObservationRepository().observationTypes().firstOrNull()?.let {
                Napier.i(tag = "ObservationFactory::init") { "Observation types fetched: $it" }
                studyObservationTypes.append(it)
            }
        }
    }

    fun addNeededObservationTypes(observationTypes: Set<String>) {
        Napier.i(tag = "ObservationFactory::addNeededObservationTypes") { "Adding observation types to studyObservationTypes: $observationTypes" }
        studyObservationTypes.append(observationTypes)
    }

    fun clearNeededObservationTypes() {
        studyObservationTypes.clear()
    }

    fun setNotificationManager(notificationManager: NotificationManager) {
        observations.forEach { it.setNotificationManager(notificationManager) }
    }

    fun observationTypes() = observations.map { it.observationType.observationType }.toSet()

    fun sensorPermissions() =
        observations.map { it.observationType.sensorPermissions }.flatten().toSet()

    fun bleDevicesNeeded(types: Set<String>): Set<String> {
        Napier.i(tag = "ObservationFactory::bleDevicesNeeded") { "Filtering types for BLE: ${studyObservationTypes.value}" }
        val bleTypes = observations.filter { it.observationType.observationType in types }
            .flatMap { it.bleDevicesNeeded() }.toSet()
        Napier.i(tag = "ObservationFactory::bleDevicesNeeded") { "BLE observation types: $bleTypes" }
        return bleTypes
    }

    fun autoStartableObservations(): Set<String> {
        val autoStartTypes = observations.filter { it.ableToAutomaticallyStart() }
            .map { it.observationType.observationType }.toSet()
        Napier.i(tag = "ObservationFactory::autoStartableObservations") { "Auto-startable observations: $autoStartTypes" }
        return autoStartTypes
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
}