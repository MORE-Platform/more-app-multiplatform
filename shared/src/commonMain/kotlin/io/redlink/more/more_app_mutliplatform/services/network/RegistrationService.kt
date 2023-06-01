package io.redlink.more.more_app_mutliplatform.services.network

import io.realm.kotlin.internal.platform.freeze
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.getPlatform
import io.redlink.more.more_app_mutliplatform.models.CredentialModel
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationConsent
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.StudyConsent
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import io.redlink.more.more_app_mutliplatform.services.store.SharedStorageRepository
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RegistrationService (
    private val shared: Shared
) {
    var study: Study? = null
        private set

    var participationToken: String? = null
    private var endpoint: String? = null

    private val scope = CoroutineScope(Job() + Dispatchers.Default)

    fun getEndpointRepository(): EndpointRepository = shared.endpointRepository

    fun sendRegistrationToken(token: String, endpoint: String? = null, onSuccess: (Study) -> Unit, onError: ((NetworkServiceError?) -> Unit), onFinish: () -> Unit) {
        if (token.isNotEmpty()) {
            Scope.launch {
                val (result, networkError) = shared.networkService.validateRegistrationToken( token.uppercase(), endpoint)
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
        Scope.launch {
            shared.credentialRepository.remove()
            shared.endpointRepository.removeEndpoint()
            DatabaseManager.deleteAll()
        }
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
                sendConsent(token, studyConsent, study, endpoint, onSuccess, onError, onFinish)
            }
        }
    }

    private fun sendConsent(token: String, studyConsent: StudyConsent, study: Study, endpoint: String? = null, onSuccess: (Boolean) -> Unit, onError: ((NetworkServiceError?) -> Unit), onFinish: () -> Unit) {
        scope.launch {
            val (config, networkError) = shared.networkService.sendConsent(token, studyConsent, endpoint)
            if (config != null) {
                config.endpoint?.let {
                    shared.endpointRepository.storeEndpoint(it)
                }
                val credentialModel =
                    CredentialModel(config.credentials.apiId, config.credentials.apiKey)
                if (shared.credentialRepository.store(credentialModel) && shared.credentialRepository.hasCredentials()) {
                    shared.resetFirstStartUp()
                    StudyRepository().storeStudy(study)
                    onSuccess(shared.credentialRepository.hasCredentials())
                } else {
                    onError(NetworkServiceError(null, "Could not store credentials").freeze())
                }
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