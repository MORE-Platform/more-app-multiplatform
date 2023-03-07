package io.redlink.more.more_app_mutliplatform.viewModels.settings

import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class CoreSettingsViewModel(
    private val credentialRepository: CredentialRepository,
    private val endpointRepository: EndpointRepository
) {
    val dataDeleted: MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val networkService: NetworkService = NetworkService(endpointRepository, credentialRepository)
    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    fun exitStudy() {
        scope.launch {
            networkService.deleteParticipation()
            credentialRepository.remove()
            endpointRepository.removeEndpoint()
            dataDeleted.value = true
        }
    }
}