package io.redlink.umm.participant.registration

import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import dev.tmapps.konnection.Konnection
import io.github.aakira.napier.Napier
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import io.redlink.umm.blendedcare.app.android.services.network.errors.NetworkServiceError
import io.redlink.umm.blendedcare.services.network.openapi.model.ObservationConsent
import io.redlink.umm.blendedcare.services.network.openapi.model.Study
import io.redlink.umm.blendedcare.services.network.openapi.model.StudyConsent
import io.redlink.umm.participant.Shared
import io.redlink.umm.participant.getPlatform
import io.redlink.umm.participant.models.CredentialModel
import io.redlink.umm.participant.models.LoginModel
import io.redlink.umm.participant.scopes.Scope
import io.redlink.umm.participant.scopes.StudyScope
import io.redlink.umm.participant.services.store.EndpointRepository
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
                        ObservationConsent(observationId = it.observationId, active = true)
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
        StudyScope.launch(Dispatchers.IO) {
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
                if (shared.credentialRepository.store(credentialModel)) {
                    val (study, error) = shared.networkService.getStudyConfig()
                    _error.value = error
                    study?.let { study ->
                        shared.observationFactory.clearNeededObservationTypes()
                        shared.repositories.study.upsert(study)
                        shared.newLogin()
                    } ?: run {
                        if (_error.value == null) {
                            _error.value = NetworkServiceError(null, "Could not get study")
                        }
                    }
                } else {
                    _error.value = NetworkServiceError(null, "Could not store credentials")
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