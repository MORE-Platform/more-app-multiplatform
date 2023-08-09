package io.redlink.more.more_app_mutliplatform

import io.github.aakira.napier.Napier
import io.github.aakira.napier.log
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.DataPointCountSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationDataSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.ScheduleSchema
import io.redlink.more.more_app_mutliplatform.database.schemas.StudySchema
import io.redlink.more.more_app_mutliplatform.extensions.asClosure
import io.redlink.more.more_app_mutliplatform.extensions.set
import io.redlink.more.more_app_mutliplatform.models.StudyState
import io.redlink.more.more_app_mutliplatform.observations.DataRecorder
import io.redlink.more.more_app_mutliplatform.observations.ObservationDataManager
import io.redlink.more.more_app_mutliplatform.observations.ObservationFactory
import io.redlink.more.more_app_mutliplatform.observations.ObservationManager
import io.redlink.more.more_app_mutliplatform.services.bluetooth.BluetoothConnector
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import io.redlink.more.more_app_mutliplatform.util.Scope
import io.redlink.more.more_app_mutliplatform.viewModels.bluetoothConnection.CoreBluetoothConnectionViewModel
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.LocalNotificationListener
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext

class Shared(
    localNotificationListener: LocalNotificationListener,
    val sharedStorageRepository: SharedStorageRepository,
    val observationDataManager: ObservationDataManager,
    val mainBluetoothConnector: BluetoothConnector,
    val observationFactory: ObservationFactory,
    val dataRecorder: DataRecorder
) {
    val endpointRepository: EndpointRepository = EndpointRepository(sharedStorageRepository)
    val credentialRepository: CredentialRepository = CredentialRepository(sharedStorageRepository)
    val networkService: NetworkService = NetworkService(endpointRepository, credentialRepository)
    val observationManager = ObservationManager(observationFactory, dataRecorder)
    val coreBluetooth = CoreBluetoothConnectionViewModel(mainBluetoothConnector)
    val notificationManager = NotificationManager(localNotificationListener, networkService)

    var appIsInForeGround = false

    val studyIsUpdating = MutableStateFlow(false)

    val currentStudyState = MutableStateFlow(StudyState.NONE)
    var finishText: String? = null

    init {
        onApplicationStart()
    }

    fun onApplicationStart() {
    }

    fun appInForeground(boolean: Boolean) {
        Napier.i { "App is in foreground: $boolean" }
        appIsInForeGround = boolean
        if (appIsInForeGround) {
            updateTaskStates()
            notificationManager.downloadMissedNotifications()
        }
    }

    fun updateTaskStates() {
        if (appIsInForeGround && credentialRepository.hasCredentials()) {
            observationManager.updateTaskStates()
        }
    }

    fun activateObservationWatcher(overwriteCheck: Boolean = false) {
        Scope.launch {
            if (overwriteCheck || StudyRepository().getStudy().firstOrNull()?.active == true) {
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

    fun showBleSetup(): Pair<Boolean, Boolean> {
        return Pair(
            firstStartUp(),
            observationFactory.bleDevicesNeeded(observationFactory.studyObservationTypes.value)
                .isNotEmpty()
        )
    }


    fun updateStudyBlocking(oldStudyState: StudyState? = null, newStudyState: StudyState? = null) {
        Scope.launch {
            updateStudy(oldStudyState, newStudyState)
        }
    }

    suspend fun updateStudy(oldStudyState: StudyState? = null, newStudyState: StudyState? = null) {
        val studyRepository = StudyRepository()
        val currentStudy = studyRepository.getStudy().firstOrNull()
        if (currentStudy != null) {
            currentStudyState.set(if (currentStudy.active) StudyState.ACTIVE else StudyState.PAUSED)
            currentStudy.finishText?.let {
                finishText = it
            }
        }
        if (newStudyState == StudyState.CLOSED || newStudyState == StudyState.PAUSED) {
            currentStudyState.set(newStudyState)
            studyIsUpdating.emit(true)
            stopObservations()
            removeStudyData()
            studyIsUpdating.emit(false)
        } else {
            val (study, error) = networkService.getStudyConfig()
            if (error != null) {
                Napier.e { error.message }
                return
            }
            study?.let { study ->
                var studyHasChanged = false
                currentStudy?.let {
                    if ((study.studyState?.let { StudyState.getState(it) } != it.getState() || it.active != study.active) || it.version != study.version) {
                        studyHasChanged = true
                    }
                }
                if (studyHasChanged || currentStudy == null) {
                    studyIsUpdating.emit(true)
                    stopObservations()
                    removeStudyData()
                    if (study.studyState?.let { StudyState.getState(it) } != StudyState.CLOSED) {
                        studyRepository.storeStudy(study)
                        resetFirstStartUp()
                        if (study.active == true) {
                            activateObservationWatcher(true)
                        }
                    }
                    if (newStudyState == null) {
                        currentStudyState.set(study.studyState?.let { StudyState.getState(it) }
                            ?: if (study.active == true) StudyState.ACTIVE else StudyState.PAUSED)
                    }
                    studyIsUpdating.emit(false)
                }
            }
            if (newStudyState != null) {
                currentStudyState.set(newStudyState)
            }
        }
    }

    fun newLogin() {
        notificationManager.newFCMToken()
        currentStudyState.set(StudyState.ACTIVE)
        Scope.launch {
            finishText = StudyRepository().getStudy().firstOrNull()?.finishText
        }
    }

    fun exitStudy(onDeletion: () -> Unit) {
        CoroutineScope(Job() + Dispatchers.Default).launch {
            stopObservations()
            observationFactory.clearNeededObservationTypes()
            networkService.deleteParticipation()
            notificationManager.deleteFCMToken()
            clearSharedStorage()
            DatabaseManager.deleteAll()
            coreBluetooth.resetAll()
            onDeletion()
            studyIsUpdating.set(false)
            currentStudyState.set(StudyState.NONE)
        }
    }

    private fun stopObservations() {
        dataRecorder.stopAll()
        observationDataManager.stopListeningToCountChanges()
    }

    private fun clearSharedStorage() {
        credentialRepository.remove()
        endpointRepository.removeEndpoint()
    }

    private suspend fun removeStudyData() {
        observationFactory.clearNeededObservationTypes()
        DatabaseManager.deleteAllFromSchema(
            setOf(
                StudySchema::class,
                ObservationSchema::class,
                ScheduleSchema::class,
                ObservationDataSchema::class,
                DataPointCountSchema::class,
                NotificationSchema::class
            )
        )
    }

    fun onStudyIsUpdatingChange(providedState: (Boolean) -> Unit) =
        studyIsUpdating.asClosure(providedState)

    fun onStudyStateChange(providedState: (StudyState) -> Unit) =
        currentStudyState.asClosure(providedState)


    companion object {
        const val FIRST_OPEN_AFTER_LOGIN_KEY = "first_open_after_login_key"
    }
}