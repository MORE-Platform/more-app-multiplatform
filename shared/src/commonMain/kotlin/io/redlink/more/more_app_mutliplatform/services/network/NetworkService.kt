package io.redlink.more.more_app_mutliplatform.services.network

import io.github.aakira.napier.Napier
import io.ktor.client.statement.*
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.ConfigurationApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.api.RegistrationApi
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.AppConfiguration
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.StudyConsent
import io.redlink.more.more_app_mutliplatform.services.store.CredentialRepository
import io.redlink.more.more_app_mutliplatform.services.store.EndpointRepository
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
) {

    private var configurationApi: ConfigurationApi? = null

    init {
        initConfigApi()
    }

    private fun initConfigApi() {
        if (configurationApi == null) {
            credentialRepository.credentials()?.let {
                val httpClient = getHttpClient()
                val url = endpointRepository.endpoint()
                configurationApi = ConfigurationApi(
                    baseUrl = url,
                    httpClient.engine,
                    httpClientConfig = { httpClient.engineConfig })
                configurationApi?.setUsername(it.apiId)
                configurationApi?.setPassword(it.apiKey)
            }
        }
    }

    suspend fun validateRegistrationToken(
        registrationToken: String,
        endpoint: String? = null
    ): Pair<Study?, NetworkServiceError?> {
        try {
            val httpClient = getHttpClient()
            val url = endpoint ?: endpointRepository.endpoint()
            val registrationApi =
                RegistrationApi(baseUrl = url, httpClientEngine = httpClient.engine)
            val registrationResponse =
                registrationApi.getStudyRegistrationInfo(moreRegistrationToken = registrationToken)
            Napier.d(registrationResponse.response.toString(), tag = TAG)
            if (registrationResponse.success) {
                registrationResponse.body().let {
                    println(it.studyTitle)
                    return Pair(it, null)
                }
            }
            println("Error; Code: ${registrationResponse.response.status.value}")
            val error = createErrorBody(
                registrationResponse.response.status.value,
                registrationResponse.response
            )
            return Pair(
                null,
                error
            )

        } catch (err: Exception) {
            err.printStackTrace()
            return Pair(null, getException(err))
        }
    }

    suspend fun sendConsent(
        registrationToken: String,
        studyConsent: StudyConsent,
        endpoint: String? = null
    ): Pair<AppConfiguration?, NetworkServiceError?> {
        try {
            val httpClient = getHttpClient()
            val url = endpoint ?: endpointRepository.endpoint()
            val registrationApi = RegistrationApi(
                baseUrl = url,
                httpClient.engine,
                httpClientConfig = { httpClient.engineConfig })
            val consentResponse = registrationApi.registerForStudy(registrationToken, studyConsent)
            if (consentResponse.success) {
                consentResponse.body().let {
                    return Pair(it, null)
                }
            }
            return Pair(
                null,
                createErrorBody(consentResponse.response.status.value, consentResponse.response)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(null, getException(e))
        }
    }

    suspend fun getStudyConfig(): Pair<Study?, NetworkServiceError?> {
        initConfigApi()
        try {
            val configResponse =
                configurationApi?.getStudyConfiguration() ?: throw Exception("Credentials not set!")
            if (configResponse.success) {
                configResponse.body().let {
                    return Pair(it, null)
                }
            }

            return Pair(
                null,
                createErrorBody(configResponse.response.status.value, configResponse.response)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Pair(null, getException(e))
        }
    }

    private fun createErrorBody(code: Int, responseBody: HttpResponse?): NetworkServiceError {
        return try {
            if (responseBody == null) {
                return NetworkServiceError(code = code, message = "Error")
            }

            val error =
                Json.decodeFromString<io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Error>(
                    responseBody.toString()
                )
            return NetworkServiceError(code = code, message = error.msg ?: "Error")
        } catch (e: Exception) {
            e.printStackTrace()
            getException(e)
        }
    }

    private fun getException(exception: Exception): NetworkServiceError {
        val errorResponse = when (exception) {
            else -> "System error!"
        }
        Napier.e("Exception: $exception", tag = TAG)
        return NetworkServiceError(null, errorResponse)
    }

    companion object {
        private const val AUTH_NAME = "apiKey"

    }
}

@ExperimentalSerializationApi
@Serializer(forClass = kotlin.Any::class)
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Any) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Any {
        return decoder.decodeString()
    }
}