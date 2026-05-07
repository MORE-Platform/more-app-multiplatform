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
import io.redlink.more.logging.EventCollection
import io.redlink.more.logging.EventObserver
import io.redlink.more.observations.appUsage.AppUsageObservation
import io.redlink.more.observations.garmin.GarminObservation
import io.redlink.more.observations.limesurvey.LimeSurveyObservation
import io.redlink.more.observations.questionObservation.QuestionObservation
import io.redlink.more.scopes.AppDispatchers
import io.redlink.more.scopes.MoreScope
import io.redlink.more.scopes.Scope
import io.redlink.more.services.notification.NotificationManager
import io.redlink.more.services.store.CredentialRepository
import io.redlink.more.services.store.PermissionRepository
import io.redlink.more.services.store.PermissionRepositoryImpl
import io.redlink.more.services.store.SharedStorageRepository
import io.redlink.more.viewModels.ViewManager
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import kotlin.reflect.KClass

abstract class ObservationFactory(
    repository: MainRepository,
    sharedStorageRepository: SharedStorageRepository,
    private val dataManager: ObservationDataManager,
    private val scope: MoreScope = Scope
) {
    private var isRequestingPermissions = false
    private var updateErrorsDeferred = false

    private var activePermissionRequests = 0

    fun startRequestingPermissions() {
        activePermissionRequests++
        isRequestingPermissions = true
    }

    fun stopRequestingPermissions() {
        activePermissionRequests--
        if (activePermissionRequests <= 0) {
            activePermissionRequests = 0
            isRequestingPermissions = false
            if (updateErrorsDeferred) {
                updateErrorsDeferred = false
                scope.launch(AppDispatchers.default) {
                    updateObservationErrors()
                }
            }
        }
    }
    private var credentialRepository: CredentialRepository? = null
    open val observations = mutableSetOf<Observation>()

    private val _studyObservationTypes: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet())
    open val studyObservationTypes: StateFlow<Set<String>> = _studyObservationTypes

    private val observationProviders = mutableSetOf<() -> Observation>()

    protected val permissionRepository = PermissionRepositoryImpl(
        sharedStorageRepository
    )

    protected var appUsageObservation: AppUsageObservation? = null

    init {
        registerImportantObservations(repository, permissionRepository)
        registerNormalObservations(repository)
        scope.launch(AppDispatchers.default) {
            repository.observation.observationTypes().collectLatest {
                Napier.i(tag = "ObservationFactory::init") { "Observation types fetched: $it" }
                initializeNeededObservations(it)
                _studyObservationTypes.value = it
                if (it.isNotEmpty()) {
                    updateObservationPermissionsAndErrorsWhenInForeground()
                }
            }
        }
    }

    private fun registerImportantObservations(
        repository: MainRepository,
        permissionRepository: PermissionRepository
    ) {
        if (appUsageObservation == null) {
            appUsageObservation = AppUsageObservation(repository, permissionRepository)
        }
        addObservationToList(appUsageObservation!!)
    }

    private fun registerNormalObservations(repository: MainRepository) {
        registerObservation { QuestionObservation(repository) }
        registerObservation { LimeSurveyObservation(repository) }
        registerObservation { GarminObservation(repository) }
    }

    open fun observationPostConstruct(observation: Observation) {}

    protected fun registerObservation(provider: () -> Observation) {
        observationProviders.add(provider)
    }

    private fun initializeNeededObservations(types: Set<String>) {
        observationProviders.forEach { provider ->
            val observation = provider()
            if (observation.observationType.matchesAny(types)) {
                if (observations.none { it.observationType.observationType == observation.observationType.observationType }) {
                    addObservationToList(
                        observation
                    )
                }
            }
        }
        Napier.d("Initialized needed observations: ${observations.map { it.observationType.observationType }}")
    }

    private fun addObservationToList(observation: Observation) {
        observations.add(
            observation
                .also { observationPostConstruct(it) }
        )
    }

    open fun addNeededObservationTypes(observationTypes: Set<String>) {
        Napier.i(tag = "ObservationFactory::addNeededObservationTypes") { "Adding observation types to studyObservationTypes: $observationTypes" }
        _studyObservationTypes.value += observationTypes
        initializeNeededObservations(_studyObservationTypes.value)
    }

    open fun clearNeededObservationTypes() {
        _studyObservationTypes.value = setOf()
        observationsWithInterface(EventObserver::class)
            .forEach { EventCollection.removeObserver(it) }
        observations.removeAll {
            !EventObserver::class.isInstance(it)
        }
        Napier.d("Cleared needed observations, but ${observations.map { it.observationType.observationType }}")
        ObservationStates.resetAll()
    }

    open fun setCredentialsRepository(credentialRepository: CredentialRepository) {
        this.credentialRepository = credentialRepository
    }

    open fun studySensorPermissions() =
        observations.filter { observationMatchesStudyTypes(it, studyObservationTypes.value) }
            .flatMap { it.observationType.sensorPermissions }.toSet()

    open fun setNotificationManager(notificationManager: NotificationManager) {
        observations.forEach { it.setNotificationManager(notificationManager) }
    }

    open fun observationTypes() =
        observations.map { it.observationType.observationType }.toSet()

    open fun getMatchingObservationTypes(types: Set<String>): Set<String> =
        observations.filter { it.observationType.matchesAny(types) }
            .map { it.observationType.observationType }.toSet()

    open fun sensorPermissions() =
        observations.map { it.observationType.sensorPermissions }.flatten().toSet()

    open fun bleDevicesNeeded(): Set<String> {
        Napier.i(tag = "ObservationFactory::bleDevicesNeeded") { "Filtering types for BLE: ${studyObservationTypes.value}" }
        val bleTypes =
            observations.filter { observationMatchesStudyTypes(it, studyObservationTypes.value) }
                .flatMap { it.bleDevicesNeeded() }.toSet()
        Napier.i(tag = "ObservationFactory::bleDevicesNeeded") { "BLE observation types: $bleTypes" }
        return bleTypes
    }

    open fun autoStartableObservations(): Set<String> {
        val autoStartTypes = studyObservations().filter { it.ableToAutomaticallyStart() }
            .map { it.observationType.observationType }.toSet()
        Napier.i(tag = "ObservationFactory::autoStartableObservations") { "Auto-startable observations: $autoStartTypes" }
        return autoStartTypes
    }

    private suspend fun updateObservationPermissionsAndErrorsWhenInForeground() {
        withTimeoutOrNull(300_000L) {
            ViewManager.appInForeground.collectLatest { inForeground ->
                if (inForeground) {
                    Napier.d { "App in foreground, updating observation permissions and errors..." }
                    updateObservationPermissions()
                    updateObservationErrors()
                    Napier.d { "Updated permission check!" }
                    cancel()
                } else {
                    var currentLogDelay = 1000L
                    while (true) {
                        Napier.d { "App not in foreground! Waiting for permission check..." }
                        delay(currentLogDelay)
                        currentLogDelay = (currentLogDelay * 2).coerceAtMost(30000L)
                    }
                }
            }
        }
    }

    open suspend fun updateObservationErrors() {
        if (isRequestingPermissions) {
            updateErrorsDeferred = true
            Napier.d { "Observation error update blocked while requesting permissions. Deferring..." }
            return
        }
        if (this.credentialRepository?.hasCredentials?.value == true) {
            Napier.d { "Updating Observation errors..." }
            studyObservations().forEach { it.updateObservationErrors() }
        }
    }

    suspend fun updateObservationPermissions() {
        if (this.credentialRepository?.hasCredentials?.value == true) {
            startRequestingPermissions()
            try {
                Napier.d { "Updating Observation permissions for types ${studyObservations()}..." }
                studyObservations().forEach {
                    yield()
                    it.updateObservationPermissions()
                }
            } finally {
                stopRequestingPermissions()
            }
        }
    }

    open fun observation(type: String): Observation? {
        Napier.i(tag = "ObservationFactory::observation") { "Fetching observation of type: $type" }
        val observation = observations.firstOrNull {
            it.observationType.matches(type)
        } ?: observationProviders.map { it() }.firstOrNull { it.observationType.matches(type) }
            ?.also {
                observations.add(it)
            }
        return observation?.apply {
            if (!this.observationDataManagerAdded()) {
                Napier.i(tag = "ObservationFactory::observation") { "Adding data manager to observation of type: $type" }
                setDataManager(dataManager)
            }
        }
    }

    fun <T : Any> observationsWithInterface(clazz: KClass<T>): Set<T> =
        observations.filter { clazz.isInstance(it) }
            .mapNotNull { it as? T }
            .toSet()

    fun onStudyExit() {
        observations.forEach { it.onStudyExit() }
        clearNeededObservationTypes()
    }

    private fun studyObservations(): List<Observation> =
        observations.filter { it.observationType.matchesAny(studyObservationTypes.value) }

    private fun observationMatchesStudyTypes(obs: Observation, types: Set<String>): Boolean =
        obs.observationType.matchesAny(types)

}