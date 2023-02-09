package io.redlink.more.more_app_mutliplatform.viewModels.permission

import io.redlink.more.more_app_multiplatform.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository

data class PermissionViewModelModel(val study: Study, val token: String, val endpoint: String? = null, val endpointRepository: EndpointRepository, val credentialRepository: CredentialRepository)