package io.redlink.more.more_app_mutliplatform.services.network.openapi.api

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.ApiClient
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.HttpResponse
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.RequestConfig
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.RequestMethod
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.map
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.wrap
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotification
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

open class NotificationApi(
    baseUrl: String = BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonSerializer: Json = JSON_DEFAULT
) : ApiClient(baseUrl, httpClientEngine, httpClientConfig, jsonSerializer) {
    open suspend fun listPushNotifications(): HttpResponse<List<PushNotification>>? {
        val localVariableAuthNames = listOf("apiKey")

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<Any?>(
            RequestMethod.GET,
            "/notifications",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true
        )

        return jsonRequest(
            localVariableConfig,
            null,
            localVariableAuthNames
        )?.wrap()
    }

    open suspend fun deleteNotification(msgId: String): HttpResponse<Unit>? {
        val localVariableAuthNames = listOf("apiKey")

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<Any?>(
            RequestMethod.DELETE,
            "/notifications/$msgId",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true
        )

        return jsonRequest(
            localVariableConfig,
            null,
            localVariableAuthNames
        )?.wrap()
    }
}