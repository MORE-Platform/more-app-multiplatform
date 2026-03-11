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

package io.redlink.more.registration

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import dev.tmapps.konnection.Konnection
import io.github.aakira.napier.Napier
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import io.redlink.more.Shared
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.getPlatform
import io.redlink.more.models.CredentialModel
import io.redlink.more.models.LoginModel
import io.redlink.more.scopes.Scope
import io.redlink.more.services.network.openapi.model.ObservationConsent
import io.redlink.more.services.network.openapi.model.Study
import io.redlink.more.services.network.openapi.model.StudyConsent
import io.redlink.more.services.store.EndpointRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.kotlincrypto.hash.md.MD5

class RegistrationService(
    private val shared: Shared,
) {
    private val _validLoginModel = MutableStateFlow<LoginModel?>(null)

    @NativeCoroutines
    val validLoginModel: StateFlow<LoginModel?> = _validLoginModel
    private val _study = MutableStateFlow<Study?>(null)

    @NativeCoroutines
    val study: StateFlow<Study?> = _study

    private val _error = MutableStateFlow<NetworkServiceError?>(null)

    @NativeCoroutines
    val error: StateFlow<NetworkServiceError?> = _error
    private val _isLoading = MutableStateFlow(false)

    @NativeCoroutines
    val isLoading: StateFlow<Boolean> = _isLoading

    private val konnection = Konnection.createInstance()

    private val _connected = MutableStateFlow(false)

    @NativeCoroutines
    val connected: StateFlow<Boolean> = _connected

    init {
        Scope.launch(Dispatchers.IO) {
            konnection.observeHasConnection().collect {
                _connected.value = it
                Napier.i("Device connected: $it")
            }
        }
    }

    fun getEndpointRepository(): EndpointRepository = shared.endpointRepository

    fun clearError() {
        _error.value = null
    }

    fun sendRegistrationToken(
        loginModel: LoginModel
    ) {
        clearError()
        _isLoading.value = true
        Scope.launch {
            val (result, networkError) = shared.networkService.validateRegistrationToken(loginModel)
            result?.let {
                _study.value = it
                _validLoginModel.value = loginModel
                addObservationPermissions(it)
            }
            _error.value = networkError
            if (networkError != null) {
                Napier.e(tag = "RegistrationService::sendRegistrationToken") { "Error sending registration token: $networkError" }
            }
        }.second.invokeOnCompletion {
            _isLoading.value = false
        }
    }

    fun acceptConsent(
        uniqueDeviceId: String,
    ) {
        clearError()
        validLoginModel.value?.let { loginModel ->
            study.value?.let { study ->
                val studyConsent = StudyConsent(
                    consent = true,
                    observations = study.observations.map {
                        ObservationConsent(
                            observationId = it.observationId,
                            active = true
                        )
                    },
                    consentInfoMD5 = MD5().digest(study.consentInfo.toByteArray())
                        .encodeBase64(),
                    deviceId = "${getPlatform().productName}#$uniqueDeviceId"
                )
                sendConsent(studyConsent)
            }
        }
    }

    private fun sendConsent(
        studyConsent: StudyConsent,
    ) {
        _isLoading.value = true
        Scope.launch(Dispatchers.IO) {
            val (config, networkError) = shared.networkService.sendConsent(
                _validLoginModel.value!!,
                studyConsent
            )
            _error.value = networkError
            if (config != null) {
                shared.credentialRepository.remove()
                shared.removeStudyData()

                config.endpoint?.let {
                    shared.endpointRepository.storeEndpoint(it)
                }
                val credentialModel =
                    CredentialModel(config.credentials.apiId, config.credentials.apiKey)
                val (study, error) = shared.networkService.getStudyConfig(credentialModel)
                _error.value = error
                study?.let { study ->
                    shared.observationFactory.clearNeededObservationTypes()
                    if (shared.credentialRepository.store(credentialModel)) {
                        shared.repositories.study.upsert(study)
                        shared.newLogin()
                    } else {
                        _error.value = NetworkServiceError(null, "Could not store credentials")
                    }
                } ?: run {
                    if (_error.value == null) {
                        _error.value = NetworkServiceError(null, "Could not get study")
                    }
                }

            }
        }.second.invokeOnCompletion {
            _isLoading.value = false
            Scope.launch(Dispatchers.IO) {
                if (shared.credentialRepository.hasCredentials.value) {
                    clearError()
                    _validLoginModel.value = null
                    _study.value = null
                }
            }
        }
    }

    fun declineConsent() {
        _study.value = null
        _validLoginModel.value = null
        clearError()
    }

    private fun addObservationPermissions(study: Study) {
        shared.observationFactory
            .addNeededObservationTypes(study.observations.map { it.observationType }.toSet())
    }
}