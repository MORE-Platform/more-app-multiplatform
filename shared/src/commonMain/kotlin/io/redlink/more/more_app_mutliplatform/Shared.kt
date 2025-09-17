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
import io.github.aakira.napier.log
import io.redlink.more.more_app_mutliplatform.database.repository.MainRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.StudyState
import io.redlink.more.more_app_mutliplatform.navigation.DeeplinkManager
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.ObservationDataManager
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Shared(
    localNotificationListener: LocalNotificationListener,
    val repositories: MainRepository,
    private val sharedStorageRepository: SharedStorageRepository,
    val observationDataManager: ObservationDataManager,
    val mainBluetoothConnector: BluetoothConnector,
    val observationFactory: ObservationFactory,
    val dataRecorder: DataRecorder
) {
    private val viewManager = ViewManager
    val deeplinkManager = DeeplinkManager(repositories, observationFactory)
    val endpointRepository = EndpointRepository(sharedStorageRepository)
    val credentialRepository = CredentialRepository(sharedStorageRepository)
    val networkService = NetworkService(endpointRepository, credentialRepository)

    val observationManager = ObservationManager(
        repositories,
        observationFactory,
        dataRecorder
    )
    val bluetoothController =
        BluetoothController(repositories.bluetoothDevice, mainBluetoothConnector)
    val notificationManager =
        NotificationManager(
            repositories,
            localNotificationListener,
            networkService,
            deeplinkManager,
            sharedStorageRepository
        )

    var appIsInForeGround = false

    val unreadNotificationCount = notificationManager.unreadUserCount

    private var bluetoothListener: Job? = null

    private val mutex = Mutex()
    private val konnection = Konnection.instance

    init {
        onApplicationStart()
        observationFactory.setCredentialsRepository(credentialRepository)
        observationFactory.setNotificationManager(notificationManager)
    }

    private fun onApplicationStart() {
        if (credentialRepository.hasCredentials.value) {
            activateObservationWatcher()
        }
    }

    fun appInForeground(boolean: Boolean) {
        if (appIsInForeGround == boolean) {
            return
        }
        Napier.i { "App is in foreground: $boolean" }
        appIsInForeGround = boolean
        if (appIsInForeGround) {
            if (credentialRepository.hasCredentials.value) {
                updateStudyBlocking()
                notificationManager.createNewFCMIfNecessary()
                bluetoothListener?.cancel()
                bluetoothListener = StudyScope.launch(Dispatchers.IO) {
                    bluetoothController.listenToConnectionChanges(
                        observationFactory
                    )
                }.second
                observationFactory.updateObservationErrors()
                updateTaskStates()
            }
            notificationManager.clearAllNotifications()
        } else {
            ViewManager.showBLEView(false)
        }
    }

    fun updateTaskStates() {
        if (appIsInForeGround && credentialRepository.hasCredentials.value) {
            observationManager.updateTaskStates()
            notificationManager.downloadMissedNotifications()
            bluetoothController.startScanningForDevices(observationFactory.bleDevicesNeeded())
        }
    }

    private fun activateObservationWatcher() {
        Scope.launch(Dispatchers.IO) {
            repositories.study.studyState.collect { state ->
                if (state == StudyState.ACTIVE) {
                    observationDataManager.listenToDatapointCountChanges()
                    updateTaskStates()
                    observationManager.activateScheduleUpdate()
                } else {
                    stopObservations()
                }
            }
        }
    }

    fun resetFirstStartUp() {
        log { "Resetting first login to true..." }
        sharedStorageRepository.store(FIRST_OPEN_AFTER_LOGIN_KEY, true)
        log {
            "Reset! First login is ${
                sharedStorageRepository.load(
                    FIRST_OPEN_AFTER_LOGIN_KEY,
                    true
                )
            }"
        }
    }

    private fun firstStartUp(): Boolean {
        return if (sharedStorageRepository.load(FIRST_OPEN_AFTER_LOGIN_KEY, true)) {
            log { "Setting first startup to false..." }
            sharedStorageRepository.store(FIRST_OPEN_AFTER_LOGIN_KEY, false)
            true
        } else false
    }

    private fun updateStudyBlocking(
        oldStudyState: StudyState? = null,
        newStudyState: StudyState? = null
    ) {
        Scope.launch(Dispatchers.IO) {
            updateStudy(oldStudyState, newStudyState)
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
                    return
                }

                val (study, error) = networkService.getStudyConfig()
                if (error != null) {
                    Napier.e { error.message }
                    return
                }
                if (study == null) {
                    Napier.d { "Study is null" }
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
                    repositories.study.upsert(study)
                    if (study.studyState?.let { StudyState.getState(it) } != StudyState.CLOSED) {
                        resetFirstStartUp()
                        observationFactory.updateObservationErrors()
                    }
                    viewManager.studyIsUpdating(false)
                } else {
                    Napier.d(tag = "Shared::updateStudy") { "No study update needed - study data is unchanged" }
                }
            }
        }
    }

    fun newLogin() {
        notificationManager.newFCMToken()
        observationFactory.updateObservationErrors()
        bluetoothListener?.cancel()
        bluetoothListener = StudyScope.launch {
            bluetoothController.listenToConnectionChanges(
                observationFactory
            )
        }.second
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

    fun clearRemainingData() {
        Napier.i { "Clearing remaining data..." }
        bluetoothController.resetAll()
        Scope.launch {
            removeStudyData()
            observationFactory.clearNeededObservationTypes()
            clearSharedStorage()
            notificationManager.clearAllNotifications()
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

    companion object {
        const val FIRST_OPEN_AFTER_LOGIN_KEY = "first_open_after_login_key"
    }
}
