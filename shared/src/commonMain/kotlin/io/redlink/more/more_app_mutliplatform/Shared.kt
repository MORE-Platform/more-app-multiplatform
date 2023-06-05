package io.redlink.more.more_app_mutliplatform

import io.github.aakira.napier.Napier
import io.redlink.more.more_app_mutliplatform.database.repository.ObservationRepository
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
import kotlinx.coroutines.flow.firstOrNull

class Shared(
    private val sharedStorageRepository: SharedStorageRepository,
    val observationDataManager: ObservationDataManager,
    val mainBluetoothConnector: BluetoothConnector,
    val observationFactory: ObservationFactory,
    val dataRecorder: DataRecorder
) {
    val endpointRepository: EndpointRepository = EndpointRepository(sharedStorageRepository)
    val credentialRepository: CredentialRepository = CredentialRepository(sharedStorageRepository)
    val networkService: NetworkService = NetworkService(endpointRepository, credentialRepository)
    val observationManager = ObservationManager(observationFactory, dataRecorder)

    var appIsInForeGround = false

    init {
        onApplicationStart()
    }
    fun onApplicationStart() {

    }

    fun appInForeground(boolean: Boolean) {
        Napier.d { "App is in foreground: $boolean" }
        appIsInForeGround = boolean
        updateTaskStates()
    }

    fun updateTaskStates() {
        if (appIsInForeGround && credentialRepository.hasCredentials()) {
            observationManager.updateTaskStates()
        }
    }

    fun activateObservationWatcher() {
        observationDataManager.listenToDatapointCountChanges()
        updateTaskStates()
        observationManager.activateScheduleUpdate()
    }

    fun resetFirstStartUp() {
        sharedStorageRepository.store(FIRST_OPEN_AFTER_LOGIN_KEY, true)
    }

    fun firstStartUp(): Boolean {
        return if (sharedStorageRepository.load(FIRST_OPEN_AFTER_LOGIN_KEY, true)) {
            sharedStorageRepository.store(FIRST_OPEN_AFTER_LOGIN_KEY, false)
            true
        } else false
    }

    fun showBleSetup(observationFactory: ObservationFactory, onCompletion: (Pair<Boolean, Boolean>) -> Unit) {
        Scope.launch {
            onCompletion(Pair(firstStartUp(), devicesNeededForObservations(observationFactory).isNotEmpty()))
        }
    }

    suspend fun devicesNeededForObservations(observationFactory: ObservationFactory): Set<String> {
        return ObservationRepository().extractExternalDevices(observationFactory).firstOrNull() ?: emptySet()
    }

    companion object {
        const val FIRST_OPEN_AFTER_LOGIN_KEY = "first_open_after_login_key"
    }
}