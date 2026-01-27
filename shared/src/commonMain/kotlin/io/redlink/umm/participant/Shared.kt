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
package io.redlink.umm.participant

import dev.tmapps.konnection.Konnection
import io.github.aakira.napier.Napier
import io.redlink.umm.participant.database.repository.MainRepository
import io.redlink.umm.participant.extensions.toStudyState
import io.redlink.umm.participant.models.StudyState
import io.redlink.umm.participant.navigation.DeeplinkManager
import io.redlink.umm.participant.observations.DataRecorder
import io.redlink.umm.participant.observations.ObservationDataManager
import io.redlink.umm.participant.observations.ObservationFactory
import io.redlink.umm.participant.observations.ObservationManager
import io.redlink.umm.participant.observations.ObservationStates
import io.redlink.umm.participant.observations.observationTypes.GarminType
import io.redlink.umm.participant.scopes.Scope
import io.redlink.umm.participant.scopes.StudyScope
import io.redlink.umm.participant.services.bluetooth.BluetoothConnector
import io.redlink.umm.participant.services.network.NetworkService
import io.redlink.umm.participant.services.notification.LocalNotificationListener
import io.redlink.umm.participant.services.notification.NotificationManager
import io.redlink.umm.participant.services.store.CredentialRepository
import io.redlink.umm.participant.services.store.EndpointRepository
import io.redlink.umm.participant.services.store.SharedStorageRepository
import io.redlink.umm.participant.viewModels.ViewManager
import io.redlink.umm.participant.viewModels.bluetoothConnection.BluetoothController
import io.redlink.umm.participant.viewModels.garminConnectOAuth.CoreGarminConnectViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi

class Shared(
    localNotificationListener: LocalNotificationListener,
    val repositories: MainRepository,
    val sharedStorageRepository: SharedStorageRepository,
    val observationDataManager: ObservationDataManager,
    mainBluetoothConnector: BluetoothConnector,
    val observationFactory: ObservationFactory,
    val dataRecorder: DataRecorder
) {
    val deeplinkManager = DeeplinkManager(repositories, observationFactory)
    val endpointRepository = EndpointRepository(sharedStorageRepository)
    val credentialRepository = CredentialRepository(sharedStorageRepository).also {
        observationFactory.setCredentialsRepository(it)
    }
    val networkService = NetworkService(endpointRepository, credentialRepository)

    val observationManager = ObservationManager(
        repositories,
        observationFactory,
        dataRecorder
    )
    val bluetoothController =
        BluetoothController(
            repositories.bluetoothDevice,
            mainBluetoothConnector,
            observationFactory = observationFactory
        )
    val notificationManager =
        NotificationManager(
            repositories,
            localNotificationListener,
            networkService,
            deeplinkManager,
            sharedStorageRepository
        )
            .also { observationFactory.setNotificationManager(it) }

    private val mutex = Mutex()
    private val konnection = Konnection.instance
    // Simple re-entrancy guard: true while updateStudyInternal is running
    @OptIn(ExperimentalAtomicApi::class)
    private val studyUpdateRunning = AtomicBoolean(false)

    init {
        Scope.launch {
            var prevFg: Boolean? = null
            var prevState: Boolean? = null
            combine(
                ViewManager.appInForeground,
                credentialRepository.hasCredentials,
                repositories.study.studyState
            ) { fg, cred, state -> Pair(fg, cred && state.isActive()) }
                .distinctUntilChanged()
                .collectLatest { (fg, state) ->
                    ViewManager.currentStudyActive(state)
                    if (fg != prevFg && (state == prevState || prevState == null && state)) {
                        Napier.d(tag = "Shared::init") { "App went to foreground: $fg, study state: $state" }
                        if (fg) {
                            updateStudy()
                            if (state) {
                                observationManager.updateTaskStates()
                                observationFactory.updateObservationErrors()
                                notificationManager.createNewFCMIfNecessary()
                                notificationManager.clearAllNotifications()
                                notificationManager.downloadMissedNotifications()
                                dataRecorder.restartAll()
                                garminLogin()
                            } else {
                                ViewManager.showBLEView(false)
                            }
                        } else {
                            ViewManager.showBLEView(false)
                        }
                    } else if (fg == prevFg && prevState != null && state != prevState) {
                        Napier.d(tag = "Shared::init") { "Study state changed: $prevState -> $state" }
                        if (state) {
                            updateStudy()
                            observationDataManager.listenToDatapointCountChanges()
                            observationManager.activateScheduleUpdate()
                            Scope.launch {
                                observationManager.updateTaskStates()
                                observationFactory.updateObservationErrors()
                            }
                            garminLogin()
                        } else {
                            stopObservations()
                            ViewManager.showBLEView(false)
                            ObservationStates.resetAll()
                        }
                    }
                    prevFg = fg
                    prevState = state
                }
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun updateStudy(
        oldStudyState: StudyState? = null,
        newStudyState: StudyState? = null
    )
    {
        if (!credentialRepository.hasCredentials.value){
            return
        }
        if (!studyUpdateRunning.compareAndSet(expectedValue = false, newValue = true)) {
            Napier.d(tag = "Shared::updateStudy") { "Study update already running - skipping call" }
            return
        }

        try {
            updateStudyInternal(oldStudyState, newStudyState)
        } finally {
            studyUpdateRunning.store(false)
        }
    }

    private suspend fun updateStudyInternal(
        oldStudyState: StudyState? = null,
        newStudyState: StudyState? = null
    ) {
        if (oldStudyState != null || newStudyState != null) {
            Napier.d(tag = "Shared::updateStudy") { "Updating study with oldState: $oldStudyState and new state: $newStudyState" }
        } else {
            Napier.d(tag = "Shared::updateStudy") { "Updating study..." }
        }

        // Handle terminal states quickly and atomically.
        if (newStudyState != null && (newStudyState == StudyState.CLOSED || newStudyState == StudyState.PAUSED)) {
            mutex.withLock {
                Napier.d(tag = "Shared::updateStudy") { "New study State is $newStudyState" }
                repositories.study.updateStudyState(newStudyState)
                StudyScope.cancel()
                notificationManager.clearAllNotifications()
            }
            return
        }

        // We avoid holding the mutex during network IO to prevent long lock contention and UI stalls.
        val currentStudyBeforeFetch = repositories.study.study.value

        if (!konnection.isConnected()) {
            Napier.d(tag = "Shared::updateStudy") { "No network connection, skipping study update" }
            if (newStudyState != null) {
                repositories.study.updateStudyState(newStudyState)
            }
            if (currentStudyBeforeFetch == null) {
                ViewManager.studyError(true)
            }
            return
        }

        // Fetch study config with timeout + small retry/backoff to smooth over flaky networks.
        // NOTE: We intentionally keep error typed as Any? to avoid coupling to the concrete error type.
        var fetchedStudy: Any? = null
        var fetchedError: Any? = null
        val maxAttempts = 3
        val timeoutMs = 10_000L
        var attempt = 0

        while (attempt < maxAttempts) {
            attempt++
            try {
                val (study, error) = withTimeout(timeoutMs) {
                    networkService.getStudyConfig()
                }
                fetchedStudy = study
                fetchedError = error

                // Success conditions
                if (error == null && study != null) {
                    break
                }

                // If we have an error or null study, we'll retry a couple times.
                val msg = when (error) {
                    is Throwable -> error.message
                    null -> "Study is null"
                    else -> error.toString()
                }
                Napier.e(tag = "Shared::updateStudy") { "Study fetch attempt $attempt/$maxAttempts failed: $msg" }
            } catch (t: Throwable) {
                fetchedError = t
                Napier.e(tag = "Shared::updateStudy") { "Study fetch attempt $attempt/$maxAttempts threw: ${t.message ?: t}" }
            }

            // Backoff before retrying
            if (attempt < maxAttempts) {
                delay(300L * attempt)
            }
        }

        val study = fetchedStudy
        val error = fetchedError

        if (error != null) {
            // Keep existing behavior: only show the loading error screen if we have no cached study.
            if (currentStudyBeforeFetch == null) {
                ViewManager.studyError(true)
            }
            return
        }

        if (study == null) {
            Napier.d(tag = "Shared::updateStudy") { "Study is null" }
            if (currentStudyBeforeFetch == null) {
                ViewManager.studyError(true)
            }
            return
        }

        // Successful fetch: ensure we leave the loading-error screen even if the study config is unchanged.
        ViewManager.studyError(false)

        // From here on we need consistency between reading the current study and applying updates.
        mutex.withLock {
            val currentStudy = repositories.study.study.value

            var studyHasChanged = false
            var stateChanged = false
            var activeStatusChanged = false
            var versionChanged = false

            currentStudy?.let { current ->
                // 'study' is Any? because we didn't tie the error type; cast locally.
                val s = study as? io.redlink.umm.blendedcare.services.network.openapi.model.Study
                val newState = s?.studyState?.toStudyState()
                val currentState = current.getState()

                if (newState != null && newState != currentState) {
                    stateChanged = true
                    studyHasChanged = true
                    Napier.d(tag = "Shared::updateStudy") { "Study state changed: $currentState -> $newState" }
                }

                if (s != null && current.active != s.active) {
                    activeStatusChanged = true
                    studyHasChanged = true
                    Napier.d(tag = "Shared::updateStudy") { "Study active status changed: ${current.active} -> ${s.active}" }
                }

                if (s != null && current.version != s.version) {
                    versionChanged = true
                    studyHasChanged = true
                    Napier.d(tag = "Shared::updateStudy") { "Study version changed: ${current.version} -> ${s.version}" }
                }
            }

            val hasNoCurrentStudy = currentStudy == null
            val shouldUpdate = studyHasChanged || hasNoCurrentStudy

            if (shouldUpdate) {
                Napier.d(tag = "Shared::updateStudy") {
                    "Study update required - hasNoCurrentStudy: $hasNoCurrentStudy, stateChanged: $stateChanged, activeStatusChanged: $activeStatusChanged, versionChanged: $versionChanged"
                }

                ViewManager.studyIsUpdating(true)
                try {
                    StudyScope.cancel()
                    observationFactory.clearNeededObservationTypes()
                    notificationManager.clearAllNotifications()
                    repositories.notification.deleteAll()

                    val s = study as io.redlink.umm.blendedcare.services.network.openapi.model.Study
                    repositories.study.upsert(s)
                } catch (e: Exception) {
                    // Failure-safe: never recurse. Just surface error state if there is no cached study.
                    Napier.e(tag = "Shared::updateStudy") { "Exception during updating study: $e" }
                    if (repositories.study.study.value == null) {
                        ViewManager.studyError(true)
                    }
                } finally {
                    ViewManager.studyIsUpdating(false)
                }
            } else {
                Napier.d(tag = "Shared::updateStudy") { "No study update needed - study data is unchanged" }
            }
        }
    }

    suspend fun newLogin() {
        notificationManager.newFCMToken()
        observationFactory.updateObservationErrors()
        garminLogin()
    }

    private fun garminLogin() {
        Scope.launch {
            Napier.d(tag = "Shared::garminLogin") { "Checking Garmin login" }
            if (observationFactory.studyObservationTypes.value
                    .contains(GarminType().observationType)
                && !sharedStorageRepository.load(
                    CoreGarminConnectViewModel.GARMIN_CONNECT_SUCCESSFUL_LOGIN,
                    false
                )
            ) {
                ViewManager.requestGarminConnectView(true)
            }
        }
    }

    fun exitStudy(onDeletion: () -> Unit) {
        StudyScope.cancel()
        bluetoothController.resetAll()
        Scope.launch {
            networkService.deleteParticipation()
            notificationManager.clearAllNotifications()
            notificationManager.deleteFCMToken()
            removeStudyData()
            repositories.notification.deleteAll()
            observationFactory.clearNeededObservationTypes()
            clearSharedStorage()
            onDeletion()
            ViewManager.resetAll()
        }
    }

    private fun stopObservations() {
        dataRecorder.stopAll()
        observationDataManager.stopListeningToCountChanges()
    }

    private fun clearSharedStorage() {
        credentialRepository.remove()
        sharedStorageRepository.remove(CoreGarminConnectViewModel.GARMIN_CONNECT_SUCCESSFUL_LOGIN)
    }

    suspend fun removeStudyData() {
        repositories.deleteAll()
        observationFactory.clearNeededObservationTypes()
    }
}
