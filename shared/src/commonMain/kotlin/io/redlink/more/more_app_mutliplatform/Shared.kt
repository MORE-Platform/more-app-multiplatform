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
package io.redlink.more.more_app_mutliplatform

import dev.tmapps.konnection.Konnection
import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.StudyState
import io.redlink.more.more_app_mutliplatform.navigation.DeeplinkManager
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.ObservationDataManager
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.observations.ObservationStates
import io.redlink.more.more_app_mutliplatform.scopes.Scope
import io.redlink.more.more_app_mutliplatform.scopes.StudyScope
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.notification.LocalNotificationListener
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import io.redlink.more.more_app_mutliplatform.viewModels.ViewManager
import io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection.BluetoothController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Shared(
    localNotificationListener: LocalNotificationListener,
    val repositories: MainRepository,
    sharedStorageRepository: SharedStorageRepository,
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
    }

    fun exitStudy(onDeletion: () -> Unit) {
        StudyScope.cancel()
        bluetoothController.resetAll()
        Scope.launch(Dispatchers.IO) {
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
    }

    suspend fun removeStudyData() {
        repositories.deleteAll()
        observationFactory.clearNeededObservationTypes()
    }

    fun unreadNotificationCountAsClosure(state: (Int) -> Unit) =
        unreadNotificationCount.asClosure(state)
}
