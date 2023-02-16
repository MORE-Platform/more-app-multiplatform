package io.redlink.more.more_app_mutliplatform.services.network

import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ConfigurationService(
    sharedStorageRepository: SharedStorageRepository
) {
    private val endpointRepository = EndpointRepository(sharedStorageRepository)
    private val credentialRepository = CredentialRepository(sharedStorageRepository)
    private val networkService: NetworkService =
        NetworkService(endpointRepository, credentialRepository)

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    fun getStudyConfig() {
        scope.launch {
            val study = networkService.getStudyConfig()
        }
    }
}