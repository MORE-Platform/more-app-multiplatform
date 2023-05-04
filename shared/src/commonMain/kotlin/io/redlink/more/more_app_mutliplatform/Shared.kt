package io.redlink.more.more_app_mutliplatform

import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository

class Shared(
    sharedStorageRepository: SharedStorageRepository
) {
    val endpointRepository: EndpointRepository = EndpointRepository(sharedStorageRepository)
    val credentialRepository: CredentialRepository = CredentialRepository(sharedStorageRepository)
    val networkService: NetworkService = NetworkService(endpointRepository, credentialRepository)
}