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
import io.redlink.more.more_app_mutliplatform.database.AppDatabase
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.models.StudyState
import io.redlink.more.more_app_mutliplatform.navigation.DeeplinkManager
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.ObservationDataManager
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.notification.LocalNotificationListener
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import io.redlink.more.more_app_mutliplatform.services.store.StudyStateRepository
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.util.StudyScope
import io.redlink.more.more_app_mutliplatform.viewModels.ViewManager
import io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection.BluetoothController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Shared(
    localNotificationListener: LocalNotificationListener,
    val database: AppDatabase,
    private val sharedStorageRepository: SharedStorageRepository,
    val observationDataManager: ObservationDataManager,
    val mainBluetoothConnector: BluetoothConnector,
    val observationFactory: ObservationFactory,
    val dataRecorder: DataRecorder
) {
    private val viewManager = ViewManager
    val deeplinkManager = DeeplinkManager(database, observationFactory)
    val endpointRepository: EndpointRepository = EndpointRepository(sharedStorageRepository)
    val credentialRepository: CredentialRepository = CredentialRepository(sharedStorageRepository)
    private val studyStateRepository: StudyStateRepository =
        StudyStateRepository(sharedStorageRepository)
    val networkService: NetworkService = NetworkService(endpointRepository, credentialRepository)
    val observationManager = ObservationManager(database, observationFactory, dataRecorder)
    val bluetoothController = BluetoothController(database, mainBluetoothConnector)
    val notificationManager =
        NotificationManager(
            database,
            localNotificationListener,
            networkService,
            deeplinkManager,
            sharedStorageRepository
        )

    var appIsInForeGround = false

    val unreadNotificationCount = notificationManager.unreadUserCount

    val currentStudyState = studyStateRepository.currentStudyState
    var finishText: String? = null
    private var bluetoothListener: Job? = null

    private val mutex = Mutex()
    private val konnection = Konnection.instance

    val studyRepository = StudyRepository(database)

    init {
        onApplicationStart()
        observationFactory.setCredentialsRepository(credentialRepository)
        observationFactory.setNotificationManager(notificationManager)
    }

    private fun onApplicationStart() {
        if (credentialRepository.hasCredentials()) {
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
            if (credentialRepository.hasCredentials()) {
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
        if (appIsInForeGround && credentialRepository.hasCredentials()) {
            observationManager.updateTaskStates()
            notificationManager.downloadMissedNotifications()
            bluetoothController.startScanningForDevices(observationFactory.bleDevicesNeeded())
        }
    }

    private fun activateObservationWatcher(overwriteCheck: Boolean = false) {
        StudyScope.launch(Dispatchers.IO) {
            if (overwriteCheck || studyRepository.getStudy()
                    .firstOrNull()?.active == true
            ) {
                observationDataManager.listenToDatapointCountChanges()
                updateTaskStates()
                observationManager.activateScheduleUpdate()
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
            val currentStudy = studyRepository.getStudy().firstOrNull()
            if (currentStudy != null) {
                Napier.d(tag = "Shared::updateStudy") { "Has current study: $currentStudy with study state: ${currentStudy.getState()} is active: ${currentStudy.active}" }
                if (currentStudyState.firstOrNull() == StudyState.NONE) {
                    studyStateRepository.storeState(currentStudy.getState())
                }
                currentStudy.finishText?.let {
                    finishText = it
                }
            }

            if (newStudyState == StudyState.CLOSED || newStudyState == StudyState.PAUSED) {
                Napier.d(tag = "Shared::updateStudy") { "New study State is $newStudyState" }
                studyStateRepository.storeState(newStudyState)
                StudyScope.cancel()
                stopObservations()
                removeStudyData()
                notificationManager.clearAllNotifications()
            } else {
                if (!konnection.isConnected()) {
                    Napier.d(tag = "Shared::updateStudy") { "No network connection, skipping study update" }
                    if (newStudyState != null) {
                        studyStateRepository.storeState(newStudyState)
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
                    stopObservations()
                    removeStudyData()
                    if (study.studyState?.let { StudyState.getState(it) } != StudyState.CLOSED) {
                        studyRepository.storeStudy(study)
                        resetFirstStartUp()
                        observationFactory.updateObservationErrors()
                    }
                    if (newStudyState == null) {
                        studyStateRepository.storeState(study.studyState?.let {
                            StudyState.getState(
                                it
                            )
                        }
                            ?: if (study.active == true) StudyState.ACTIVE else StudyState.PAUSED)
                    }
                    if (study.active == true) {
                        activateObservationWatcher(true)
                    }
                    viewManager.studyIsUpdating(false)
                } else {
                    Napier.d(tag = "Shared::updateStudy") { "No study update needed - study data is unchanged" }
                }

                if (newStudyState != null) {
                    studyStateRepository.storeState(newStudyState)
                }
            }
        }
    }

    fun newLogin() {
        notificationManager.newFCMToken()
        studyStateRepository.storeState(StudyState.ACTIVE)
        StudyScope.launch {
            finishText = studyRepository.getStudy().firstOrNull()?.finishText
        }
        activateObservationWatcher()
        updateTaskStates()
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
        stopObservations()
        bluetoothController.resetAll()
        Scope.launch(Dispatchers.IO) {
            networkService.deleteParticipation()
            notificationManager.clearAllNotifications()
            notificationManager.deleteFCMToken()
            removeStudyData()
            clearSharedStorage()
            onDeletion()
            observationFactory.clearNeededObservationTypes()
            viewManager.resetAll()
            studyStateRepository.storeState(StudyState.NONE)
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
        database.studyDao().deleteAll()
        database.observationDao().deleteAll()
        database.scheduleDao().deleteAll()
        database.observationDataDao().deleteAll()
        database.dataPointDao().deleteAll()
        database.notificationDao().deleteAll()
        observationFactory.clearNeededObservationTypes()
    }

    fun onStudyStateChange(providedState: (StudyState) -> Unit) =
        currentStudyState.asClosure(providedState)

    fun unreadNotificationCountAsClosure(state: (Int) -> Unit) =
        unreadNotificationCount.asClosure(state)

    companion object {
        const val FIRST_OPEN_AFTER_LOGIN_KEY = "first_open_after_login_key"
    }
}