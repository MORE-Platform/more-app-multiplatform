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
package io.redlink.more

import dev.icerock.moko.resources.desc.Resource
import dev.icerock.moko.resources.desc.StringDesc
import dev.tmapps.konnection.Konnection
import io.github.aakira.napier.Napier
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.extensions.toStudyState
import io.redlink.more.models.StudyState
import io.redlink.more.navigation.DeeplinkManager
import io.redlink.more.navigation.DeeplinkManagerImpl
import io.redlink.more.observations.DataRecorder
import io.redlink.more.observations.ObservationDataManager
import io.redlink.more.observations.ObservationFactory
import io.redlink.more.observations.ObservationManager
import io.redlink.more.observations.ObservationStates
import io.redlink.more.observations.observationTypes.GarminType
import io.redlink.more.scopes.Scope
import io.redlink.more.scopes.StudyScope
import io.redlink.more.services.ObservationService
import io.redlink.more.services.bluetooth.BluetoothConnector
import io.redlink.more.services.network.NetworkService
import io.redlink.more.services.network.NetworkServiceImpl
import io.redlink.more.services.network.openapi.model.Study
import io.redlink.more.services.notification.LocalNotificationListener
import io.redlink.more.services.notification.NotificationActionObserver
import io.redlink.more.services.notification.NotificationManager
import io.redlink.more.services.store.CredentialRepository
import io.redlink.more.services.store.CredentialRepositoryImpl
import io.redlink.more.services.store.EndpointRepository
import io.redlink.more.services.store.EndpointRepositoryImpl
import io.redlink.more.services.store.SharedStorageRepository
import io.redlink.more.viewModels.ViewManager
import io.redlink.more.viewModels.bluetoothConnection.BluetoothController
import io.redlink.more.viewModels.garminConnectOAuth.CoreGarminConnectViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

open class Shared(
    localNotificationListener: LocalNotificationListener,
    val repositories: MainRepository,
    val sharedStorageRepository: SharedStorageRepository,
    val observationDataManager: ObservationDataManager,
    mainBluetoothConnector: BluetoothConnector,
    val observationFactory: ObservationFactory,
    val dataRecorder: DataRecorder,
    reminderNotificationSchedulingLimit: Int? = null,
    val connectionStatusFlow: Flow<Boolean> =
        Konnection.createInstance().observeHasConnection()
) : NotificationActionObserver, AutoCloseable {
    val deeplinkManager: DeeplinkManager = DeeplinkManagerImpl(repositories, observationFactory)
    val endpointRepository: EndpointRepository = EndpointRepositoryImpl(sharedStorageRepository)
    val credentialRepository: CredentialRepository =
        CredentialRepositoryImpl(sharedStorageRepository).also {
            observationFactory.setCredentialsRepository(it)
        }
    val networkService: NetworkService =
        NetworkServiceImpl(endpointRepository, credentialRepository)

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
            .also { it.setActionObserver(this) }
            .also { observationFactory.setNotificationManager(it) }

    val observationService =
        ObservationService(repositories, notificationManager, reminderNotificationSchedulingLimit)

    private val mutex = Mutex()
    private var mainJob: Job? = null

    init {
        val handler = CoroutineExceptionHandler { _, t ->
            Napier.e(tag = "Shared::init") { "Init watcher crashed: ${t.stackTraceToString()}" }
        }

        mainJob?.cancel()
        mainJob = Scope.launch(handler) {
            while (isActive) {
                try {
                    combine(
                        credentialRepository.hasCredentials,
                        repositories.study.studyState
                    ) { cred, state -> cred && state.isActive() }
                        .catch { e ->
                            Napier.e(tag = "Shared::init") { "Foreground/study watcher failed: ${e.stackTraceToString()}" }
                            if (e is CancellationException) throw e
                        }
                        .distinctUntilChanged()
                        .collect { state ->
                            ViewManager.currentStudyActive(state)
                            if (state) {
                                updateData(ViewManager.appInForeground.value)
                            } else {
                                stopObservations()
                                ViewManager.showBLEView(false)
                                ObservationStates.resetAll()
                            }
                        }

                    // If collect ever returns normally, we restart the loop.
                    Napier.w(tag = "Shared::init") { "Foreground/study watcher completed unexpectedly; restarting" }
                    delay(500L)
                } catch (e: CancellationException) {
                    // Normal shutdown/cancel.
                    Napier.i(tag = "Shared::init") { "Foreground/study watcher cancelled" }
                    throw e
                } catch (t: Throwable) {
                    // Any exception inside the collector would previously cancel the coroutine silently.
                    Napier.e(tag = "Shared::init") { "Foreground/study watcher crashed; restarting: ${t.stackTraceToString()}" }
                    delay(1000L)
                }
            }
        }.second
    }

    fun updateData(appInForeground: Boolean) {
        ViewManager.appIsInForeground(appInForeground)
        Scope.launch {
            Napier.d(tag = "Shared:updateData") { "Updating data, with app in foreground: $appInForeground" }
            if (appInForeground) {
                notificationManager.createNewFCMIfNecessary()
                updateStudy()
                if (repositories.study.studyState.value == StudyState.ACTIVE) {
                    notificationManager.createNewFCMIfNecessary()
                    updateSchedules()
                    withContext(Dispatchers.Main) {
                        observationDataManager.listenToDatapointCountChanges()
                        observationManager.activateScheduleUpdate()
                        dataRecorder.restartAll()
                    }
                    garminLogin()
                    notificationManager.clearAllNotifications()
                } else {
                    ViewManager.showBLEView(false)
                }
                notificationManager.clearAllNotifications()
            } else {
                ViewManager.showBLEView(false)
                if (repositories.study.studyState.value == StudyState.ACTIVE) {
                    observationDataManager.sendData(true)
                }
            }
        }
    }

    suspend fun updateSchedules() {
        observationManager.updateTaskStates()
        observationFactory.updateObservationErrors()
        observationService.scheduleObservationReminder()
        notificationManager.downloadMissedNotifications()
    }

    override fun updateStudy(
        oldStudyState: StudyState?,
        newStudyState: StudyState?
    ) {
        if (!credentialRepository.hasCredentials.value || mutex.isLocked) {
            return
        }
        Scope.launch {
            mutex.withLock {
                updateStudyInternal(oldStudyState, newStudyState)
            }
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

        if (newStudyState != null && (newStudyState == StudyState.CLOSED || newStudyState == StudyState.PAUSED)) {
            Napier.d(tag = "Shared::updateStudy") { "New study State is $newStudyState" }
            repositories.study.updateStudyState(newStudyState)
            StudyScope.cancel()
            observationService.clearReminders()
            notificationManager.clearAllNotifications()
            return
        }

        val currentStudyBeforeFetch = repositories.study.getStudy().firstOrNull()

        if (connectionStatusFlow.firstOrNull() == false) {
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
        var attempt = 0

        while (attempt < maxAttempts) {
            attempt++
            try {
                val (study, error) = networkService.getStudyConfig()

                fetchedStudy = study
                fetchedError = error

                if (error == null && study != null) {
                    break
                }

                val msg = when (error) {
                    null -> "Study is null"
                    else -> error.toString()
                }
                Napier.e(tag = "Shared::updateStudy") { "Study fetch attempt $attempt/$maxAttempts failed: $msg" }
            } catch (t: Throwable) {
                fetchedError = t
                Napier.e(tag = "Shared::updateStudy") { "Study fetch attempt $attempt/$maxAttempts threw: ${t.message ?: t}" }
            }

            if (attempt < maxAttempts) {
                delay(300L * attempt)
            }
        }

        val study = fetchedStudy
        val error = fetchedError

        if (error != null) {
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

        ViewManager.studyError(false)


        var studyHasChanged = false
        var stateChanged = false
        var activeStatusChanged = false
        var versionChanged = false

        currentStudyBeforeFetch?.let { current ->
            val s = study as? Study
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

        val hasNoCurrentStudy = currentStudyBeforeFetch == null
        val shouldUpdate = studyHasChanged || hasNoCurrentStudy

        if (shouldUpdate) {
            Napier.d(tag = "Shared::updateStudy") {
                "Study update required - hasNoCurrentStudy: $hasNoCurrentStudy, stateChanged: $stateChanged, activeStatusChanged: $activeStatusChanged, versionChanged: $versionChanged"
            }

            ViewManager.studyIsUpdating(true)
            try {
                StudyScope.cancel()
                observationFactory.clearNeededObservationTypes()
                observationService.clearReminders()
                notificationManager.clearAllNotifications()
                repositories.notification.deleteAll()

                val s = study as Study
                withContext(Dispatchers.Main) {
                    repositories.study.upsert(s)
                }
                updateSchedules()
            } catch (e: Exception) {
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

    suspend fun newLogin() {
        notificationManager.newFCMToken()
        observationFactory.updateObservationErrors()
        garminLogin()
    }

    private fun garminLogin() {
        Scope.launch {
            Napier.d(tag = "Shared::garminLogin") { "Checking Garmin login" }
            val garminType = GarminType()
            if (garminType.matchesAny(observationFactory.studyObservationTypes.value)
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
            notificationManager.deleteFCMToken()
            observationService.clearReminders()
            notificationManager.clearAllNotifications()
            removeStudyData()
            observationFactory.clearNeededObservationTypes()
            onDeletion()
            ViewManager.resetAll()
            clearSharedStorage()
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

    private fun shutdown() {
        notificationManager.setActionObserver(null)
    }

    override fun close() {
        shutdown()
    }

    companion object {
        val PROTOCOL = StringDesc.Resource(SharedRes.strings.deeplink_protocol)
        val HOST = StringDesc.Resource(SharedRes.strings.deeplink_host)
    }
}
