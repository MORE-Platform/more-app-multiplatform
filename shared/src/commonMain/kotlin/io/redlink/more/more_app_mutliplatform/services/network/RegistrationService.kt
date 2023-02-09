package io.redlink.more.more_app_mutliplatform.services.network

import io.ktor.utils.io.core.*
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_multiplatform.openapi.model.ObservationConsent
import io.redlink.more.more_app_multiplatform.openapi.model.Study
import io.redlink.more.more_app_multiplatform.openapi.model.StudyConsent
import io.redlink.more.more_app_mutliplatform.getPlatform
import io.redlink.more.more_app_mutliplatform.models.CredentialModel
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RegistrationService(
    sharedStorageRepository: SharedStorageRepository
) {
    private val endpointRepository = EndpointRepository(sharedStorageRepository)
    private val credentialRepository = CredentialRepository(sharedStorageRepository)
    private val networkService: NetworkService = NetworkService(endpointRepository, credentialRepository)

    var study: Study? = null
        private set

    private var participationToken: String? = null
    private var endpoint: String? = null

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    fun getEndpointRepository(): EndpointRepository = endpointRepository

    fun sendRegistrationToken(token: String, endpoint: String? = null, onSuccess: (Study) -> Unit, onError: ((NetworkServiceError?) -> Unit), onFinish: () -> Unit) {
        if (token.isNotEmpty()) {
            val upperCaseToken = token.uppercase()
            scope.launch {
                val (result, networkError) = networkService.validateRegistrationToken(upperCaseToken, endpoint)
                result?.let {
                    study = it
                    participationToken = token
                    onSuccess(it)
                }
                networkError?.let {
                    onError(networkError)
                }
                onFinish()
            }
        }
    }

    fun acceptConsent(consentInfoMd5: String, uniqueDeviceId: String, onSuccess: (Boolean) -> Unit, onError: ((NetworkServiceError?) -> Unit), onFinish: () -> Unit) {
        study?.let { study ->
            participationToken?.let {token ->
                val studyConsent = StudyConsent(
                    consent = true,
                    observations = study.observations.map {
                        ObservationConsent(observationId = it.observationId, active = true)
                    },
                    consentInfoMD5 = consentInfoMd5,
                    deviceId = "${getPlatform().productName}#$uniqueDeviceId"
                )
                sendConsent(token, studyConsent, endpoint, onSuccess, onError, onFinish)
            }
        }
    }

    private fun sendConsent(token: String, studyConsent: StudyConsent, endpoint: String? = null, onSuccess: (Boolean) -> Unit, onError: ((NetworkServiceError?) -> Unit), onFinish: () -> Unit) {
        scope.launch {
            val (config, networkError) = networkService.sendConsent(token, studyConsent, endpoint)
            if (config != null) {
                config.endpoint?.let {
                    endpointRepository.storeEndpoint(it)
                }
                val credentialModel =
                    CredentialModel(config.credentials.apiId, config.credentials.apiKey)
                credentialRepository.store(credentialModel)
                onSuccess(credentialRepository.hasCredentials())
            }
            networkError?.let {
                onError(it)
            }
            onFinish()
        }
    }


    fun reset() {
        study = null
        participationToken = null
    }

}