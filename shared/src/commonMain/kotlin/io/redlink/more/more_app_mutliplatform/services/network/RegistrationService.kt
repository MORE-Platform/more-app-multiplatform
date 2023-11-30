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
package io.redlink.more.more_app_mutliplatform.services.network

import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.Shared
import io.redlink.more.more_app_mutliplatform.database.DatabaseManager
import io.redlink.more.more_app_mutliplatform.database.repository.StudyRepository
import io.redlink.more.more_app_mutliplatform.getPlatform
import io.redlink.more.more_app_mutliplatform.models.CredentialModel
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationConsent
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.StudyConsent
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
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

    fun sendRegistrationToken(token: String, manualEndpoint: String? = null, onSuccess: (Study) -> Unit, onError: ((NetworkServiceError?) -> Unit), onFinish: () -> Unit) {
        if (token.isNotEmpty()) {
            Scope.launch {
                val (result, networkError) = shared.networkService.validateRegistrationToken( token.uppercase(), manualEndpoint)
                result?.let {
                    endpoint = manualEndpoint
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
                    StudyRepository().storeStudy(study)
                    shared.observationFactory.addNeededObservationTypes(study.observations.map { it.observationType }.toSet())
                    shared.resetFirstStartUp()
                    onSuccess(shared.credentialRepository.hasCredentials())
                } else {
                    onError(NetworkServiceError(null, "Could not store credentials"))
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