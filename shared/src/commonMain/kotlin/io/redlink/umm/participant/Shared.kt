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
import io.redlink.umm.participant.extensions.asClosure
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Shared(
    localNotificationListener: LocalNotificationListener,
    val repositories: MainRepository,
    val sharedStorageRepository: SharedStorageRepository,
    val observationDataManager: ObservationDataManager,
    mainBluetoothConnector: BluetoothConnector,
    val observationFactory: ObservationFactory,
    val dataRecorder: DataRecorder
) {
    private val viewManager = ViewManager
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

    val unreadNotificationCount = notificationManager.unreadUserCount

    private val mutex = Mutex()
    private val konnection = Konnection.instance

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
                        if (fg && state) {
                            updateStudy()
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
                    } else if (fg == prevFg && prevState != null && state != prevState) {
                        Napier.d(tag = "Shared::init") { "Study state changed: $prevState -> $state" }
                        if (state) {
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

    /**
     * Updates the study state and related resources. This function ensures that any change in the
     * current study, study state, or study configuration is appropriately handled. It synchronizes
     * updates using a lock and takes necessary actions such as storing the new state, updating study
     * resources, managing notifications, and handling changes in connectivity.
     *
     * @param oldStudyState The previous state of the study. Can be null if there is no specific old state to compare with.
     * @param newStudyState The new state to transition the study to. Can be null if no state change is required.
     */
    suspend fun updateStudy(
        oldStudyState: StudyState? = null,
        newStudyState: StudyState? = null
    ) {
        mutex.withLock {
            if (oldStudyState != null || newStudyState != null) {
                Napier.d(tag = "Shared::updateStudy") { "Updating study with oldState: $oldStudyState and new state: $newStudyState" }
            } else {
                Napier.d(tag = "Shared::updateStudy") { "Updating study..." }
            }
            val currentStudy = repositories.study.study.value
            if (newStudyState == StudyState.CLOSED || newStudyState == StudyState.PAUSED) {
                Napier.d(tag = "Shared::updateStudy") { "New study State is $newStudyState" }
                repositories.study.updateStudyState(newStudyState)
                StudyScope.cancel()
                notificationManager.clearAllNotifications()
            } else {
                if (!konnection.isConnected()) {
                    Napier.d(tag = "Shared::updateStudy") { "No network connection, skipping study update" }
                    if (newStudyState != null) {
                        repositories.study.updateStudyState(newStudyState)
                    }
                    if (currentStudy == null) {
                        ViewManager.studyError(true)
                    }
                    return
                }

                val (study, error) = networkService.getStudyConfig()
                if (error != null) {
                    Napier.e { error.message }
                    if (currentStudy == null) {
                        ViewManager.studyError(true)
                    }
                    return
                }
                if (study == null) {
                    Napier.d { "Study is null" }
                    if (currentStudy == null) {
                        ViewManager.studyError(true)
                    }
                    return
                }

                var studyHasChanged = false
                var stateChanged = false
                var activeStatusChanged = false
                var versionChanged = false

                currentStudy?.let { current ->
                    val newState = study.studyState?.let { StudyState.getState(it) }
                    val currentState = current.getState()

                    if (newState != currentState) {
                        stateChanged = true
                        studyHasChanged = true
                        Napier.d(tag = "Shared::updateStudy") { "Study state changed: $currentState -> $newState" }
                    }

                    if (current.active != study.active) {
                        activeStatusChanged = true
                        studyHasChanged = true
                        Napier.d(tag = "Shared::updateStudy") { "Study active status changed: ${current.active} -> ${study.active}" }
                    }

                    if (current.version != study.version) {
                        versionChanged = true
                        studyHasChanged = true
                        Napier.d(tag = "Shared::updateStudy") { "Study version changed: ${current.version} -> ${study.version}" }
                    }
                }

                val hasNoCurrentStudy = currentStudy == null
                val shouldUpdate = studyHasChanged || hasNoCurrentStudy

                if (shouldUpdate) {
                    Napier.d(tag = "Shared::updateStudy") { "Study update required - hasNoCurrentStudy: $hasNoCurrentStudy, stateChanged: $stateChanged, activeStatusChanged: $activeStatusChanged, versionChanged: $versionChanged" }

                    viewManager.studyIsUpdating(true)
                    StudyScope.cancel()
                    observationFactory.clearNeededObservationTypes()
                    notificationManager.clearAllNotifications()
                    repositories.notification.deleteAll()
                    ViewManager.studyError(false)
                    repositories.study.upsert(study)
                    viewManager.studyIsUpdating(false)
                } else {
                    Napier.d(tag = "Shared::updateStudy") { "No study update needed - study data is unchanged" }
                }
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
            if (observationFactory.observationTypes()
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
            viewManager.resetAll()
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

    fun unreadNotificationCountAsClosure(state: (Int) -> Unit) =
        unreadNotificationCount.asClosure(state)
}
