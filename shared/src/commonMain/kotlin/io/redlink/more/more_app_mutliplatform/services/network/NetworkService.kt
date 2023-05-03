package io.redlink.more.more_app_mutliplatform.services.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.statement.*
import io.ktor.utils.io.core.Closeable
import io.realm.kotlin.internal.platform.freeze
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.ConfigurationApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.DataApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.RegistrationApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.*
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
import kotlinx.coroutines.CompletionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

private const val TAG = "NetworkService"

class NetworkService(
    private val endpointRepository: EndpointRepository,
    private val credentialRepository: CredentialRepository
): Closeable {
    private var httpClient: HttpClient? = null
    private var configurationApi: ConfigurationApi? = null
    private var dataApi: DataApi? = null

    private var engineUseCounter = 10

    private fun initConfigApi() {
        if (configurationApi == null) {
            Napier.d { "Initializing ConfigurationApi and DataApi..." }
            credentialRepository.credentials()?.let {
                httpClient = getHttpClient().apply {
                    val url = endpointRepository.endpoint()
                    configurationApi = ConfigurationApi(
                        baseUrl = url,
                        engine,
                        httpClientConfig = { engineConfig })
                    configurationApi?.setUsername(it.apiId)
                    configurationApi?.setPassword(it.apiKey)
                }
            }
        }
    }

    private fun initDataApi() {
        if (--engineUseCounter <= 0) {
            close()
        }
        if (dataApi == null) {
            credentialRepository.credentials()?.let {
                httpClient = getHttpClient().apply {
                    dataApi = DataApi(
                        baseUrl = endpointRepository.endpoint(),
                        engine,
                        httpClientConfig = { engineConfig}
                    )
                    dataApi?.setUsername(it.apiId)
                    dataApi?.setPassword(it.apiKey)
                }
            }
        }
    }

    suspend fun deleteParticipation(): Pair<Boolean, NetworkServiceError?> {
        try {
            credentialRepository.credentials()?.let {
                Napier.d { "Deleting Participation..." }
                val httpClient = getHttpClient()
                val url =  endpointRepository.endpoint()
                val registrationApi =
                    RegistrationApi(baseUrl = url, httpClientEngine = httpClient.engine)

                registrationApi.setUsername(it.apiId)
                registrationApi.setPassword(it.apiKey)

                val registrationResponse =
                    registrationApi.unregisterFromStudy()
                Napier.d(registrationResponse.response.toString(), tag = TAG)
                close()
                if (registrationResponse.success) {
                    Napier.d { "Participation deleted!" }
                    return Pair(true, null)
                }
                println("Error; Code: ${registrationResponse.response.status.value}")
                val error = createErrorBody(
                    registrationResponse.response.status.value,
                    registrationResponse.response
                )
                return Pair(
                    false,
                    error
                )
            }
            return Pair(false, NetworkServiceError(null, "No credentials"))
        } catch (err: Exception) {
            err.printStackTrace()
            return Pair(false, getException(err))
        }
    }

    suspend fun validateRegistrationToken(
        registrationToken: String,
        endpoint: String? = null
    ): Pair<Study?, NetworkServiceError?> {
        try {
            Napier.d { "Validating Registration token..." }
            val httpClient = getHttpClient()
            val url = endpoint ?: endpointRepository.endpoint()
            val registrationApi =
                RegistrationApi(baseUrl = url, httpClientEngine = httpClient.engine)
            val registrationResponse =
                registrationApi.getStudyRegistrationInfo(moreRegistrationToken = registrationToken)
            Napier.d(registrationResponse.response.toString(), tag = TAG)
            if (registrationResponse.success) {
                registrationResponse.body().let {
                    Napier.d { "Registration token valid!" }
                    return Pair(it, null).freeze()
                }
            }
            val error = createErrorBody(
                registrationResponse.response.status.value,
                registrationResponse.response
            )
            return Pair(
                null,
                error
            )

        } catch (err: Exception) {
            return Pair(null, getException(err))
        }
    }

    suspend fun sendConsent(
        registrationToken: String,
        studyConsent: StudyConsent,
        endpoint: String? = null
    ): Pair<AppConfiguration?, NetworkServiceError?> {
        try {
            Napier.d { "Sending Consent..." }
            val httpClient = getHttpClient()
            val url = endpoint ?: endpointRepository.endpoint()
            val registrationApi = RegistrationApi(
                baseUrl = url,
                httpClient.engine,
                httpClientConfig = { httpClient.engineConfig })
            val consentResponse = registrationApi.registerForStudy(registrationToken, studyConsent)
            if (consentResponse.success) {
                consentResponse.body().let {
                    Napier.d { "Credentials received!" }
                    return Pair(it, null).freeze()
                }
            }
            return Pair(
                null,
                createErrorBody(consentResponse.response.status.value, consentResponse.response)
            )
        } catch (e: Exception) {
            return Pair(null, getException(e))
        }
    }

    suspend fun getStudyConfig(): Pair<Study?, NetworkServiceError?> {
        initConfigApi()
        try {
            Napier.d { "Downloading study data..." }
            val configResponse =
                configurationApi?.getStudyConfiguration() ?: return Pair(null, NetworkServiceError(null, "No credentials set!"))
            if (configResponse.success) {
                configResponse.body().let {
                    Napier.d { "Loading study data success!" }
                    return Pair(it, null)
                }
            }

            return Pair(
                null,
                createErrorBody(configResponse.response.status.value, configResponse.response)
            )
        } catch (e: Exception) {
            return Pair(null, getException(e))
        }
    }

    suspend fun sendNotificationToken(token: String): Pair<Boolean, NetworkServiceError?> {
        initConfigApi()
        configurationApi?.let {
            try {
                Napier.d { "Sending notification token..." }
                val tokenResponse = it.setPushNotificationToken(
                    serviceType = PushNotificationServiceType.FCM,
                    pushNotificationToken = PushNotificationToken(token = token)
                )
                if (tokenResponse.success) {
                    Napier.d { "Uploading notification token success!" }
                    return Pair(true, null)
                }
                return Pair(
                    false,
                    createErrorBody(tokenResponse.status, tokenResponse.response)
                )
            } catch (err: Exception) {
                return Pair(false, getException(err))
            }
        }
        return Pair(false, getException(Exception("No credentials found!")))
    }

    suspend fun sendData(data: DataBulk): Pair<Set<String>, NetworkServiceError?> {
        initDataApi()
        try {
            Napier.i { "Sending bulk ${data.bulkId} with ${data.dataPoints.size} datapoints wwith first being ${data.dataPoints.first()}..." }
            val dataApiResponse = dataApi?.storeBulk(data) ?: return Pair(
                emptySet(),
                NetworkServiceError(null, "No credentials set!")
            )
            if (dataApiResponse.success) {
                dataApiResponse.body().freeze().let {
                    Napier.d { "Sent data!" }
                    return Pair(it.toSet().freeze(), null).freeze()
                }
            }
            return Pair(
                emptySet(),
                createErrorBody(dataApiResponse.response.status.value, dataApiResponse.response)
            )
        } catch (e: Exception) {
            return Pair(emptySet(), getException(e))
        }
    }

    fun sendData(data: DataBulk, completionHandler: (Pair<Set<String>, NetworkServiceError?>) -> Unit) {
        CoroutineScope(Job() + Dispatchers.Default).launch {
            completionHandler(sendData(data).apply { first.freeze() })
        }
    }

    private fun createErrorBody(code: Int, responseBody: HttpResponse?): NetworkServiceError {
        return try {
            if (responseBody == null) {
                return NetworkServiceError(code = code, message = "Error")
            }
            val error =
                Json.decodeFromString<Error>(
                    responseBody.toString()
                )
            NetworkServiceError(code = code, message = error.msg ?: "Error")
        } catch (e: Exception) {
            getException(e)
        }
    }

    private fun getException(exception: Exception): NetworkServiceError {
        val errorResponse = when (exception) {
            else -> "System error!"
        }
        Napier.e("Exception: ${exception.stackTraceToString()}", tag = TAG)
        exception.printStackTrace()
        return NetworkServiceError(null, errorResponse)
    }

    override fun close() {
        Napier.d { "Clearing the Http engine..." }
        engineUseCounter = 10
        configurationApi = null
        dataApi = null
        httpClient?.close()
        httpClient = null
    }
}

