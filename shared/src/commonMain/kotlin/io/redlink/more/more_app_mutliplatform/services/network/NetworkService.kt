package io.redlink.more.more_app_mutliplatform.services.network

import io.github.aakira.napier.Napier
import io.redlink.more.app.android.services.network.errors.NetworkServiceError
import io.redlink.more.more_app_multiplatform.openapi.api.RegistrationApi
import io.redlink.more.more_app_multiplatform.openapi.infrastructure.HttpResponse
import io.redlink.more.more_app_multiplatform.openapi.model.Study
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

class NetworkService(endpointRepository: EndpointRepository, credentialRepository: CredentialRepository) {

    suspend fun validateRegistrationToken(registrationToken: String): Pair<Study?, NetworkServiceError?> {
        try {
            val httpClient = getHttpClient()

            val registrationApi =
                RegistrationApi(baseUrl = DATA_BASE_PATH_ENDPOINT, httpClientEngine = httpClient.engine)
            val registrationResponse = registrationApi.getStudyRegistrationInfo(moreRegistrationToken = registrationToken)
            Napier.d(registrationResponse.response.toString(), tag = TAG)
            if (registrationResponse.success) {
                registrationResponse.body().let {
                    return Pair(it, null)
                }
            }

//            val error = createErrorBody(registrationResponse.response.status.value, "registrationResponse")
            val error = NetworkServiceError(registrationResponse.response.status.value, "Error")
            return Pair(
                null,
                error
            )

        } catch (err: Exception) {
            err.printStackTrace()
            return Pair(null, getException(err))
        }
    }

    private fun createErrorBody(code: Int, responseBody: HttpResponse<Any>?): NetworkServiceError {
        return try {
            if (responseBody == null){
                return NetworkServiceError(code = code, message = "Error")
            }

            val error = Json.decodeFromString<io.redlink.more.more_app_multiplatform.openapi.model.Error>(responseBody.toString())
            return NetworkServiceError(code = code, message = error.msg ?: "Error")
        } catch (e: Exception) {
            e.printStackTrace()
            Napier.e(e.toString(), tag = TAG)
            NetworkServiceError(code = code, "Error")
        }
    }

    private fun getException(exception: Exception): NetworkServiceError {
        val errorResponse = when (exception) {
            else -> "Error"
        }
        Napier.e( "Exception: $exception", tag = TAG)
        return NetworkServiceError(null, errorResponse)
    }

    companion object {
        private const val AUTH_NAME = "apiKey"

        private const val DATA_BASE_PATH_ENDPOINT: String =
            "https://data.platform-test.more.redlink.io/api/v1"

    }
}

@ExperimentalSerializationApi
@Serializer(forClass = kotlin.Any::class)
object AnySerializer: KSerializer<Any> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("Any", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Any) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): Any {
        return decoder.decodeString()
    }
}