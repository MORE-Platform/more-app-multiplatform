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
@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.redlink.more.more_app_mutliplatform.services.network.openapi.api

import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.ApiClient
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.HttpResponse
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.RequestConfig
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.RequestMethod
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.map
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.wrap
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotificationConfig
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotificationServiceType
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotificationToken
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

open class ConfigurationApi(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonSerializer: Json = ApiClient.JSON_DEFAULT
) : ApiClient(baseUrl, httpClientEngine, httpClientConfig, jsonSerializer) {

    /**
     * 
     * retrieve the client-configuration for the push-notification service
     * @param serviceType 
     * @return PushNotificationConfig
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getPushNotificationServiceClientConfig(serviceType: PushNotificationServiceType): HttpResponse<PushNotificationConfig>? {

        val localVariableAuthNames = listOf<String>("apiKey")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<Any?>(
            RequestMethod.GET,
            "/config/notifications/{serviceType}".replace("{" + "serviceType" + "}", "$serviceType"),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        )?.wrap()
    }


    /**
     * 
     * (re)load the study configuration
     * @return Study
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getStudyConfiguration(): HttpResponse<Study>? {

        val localVariableAuthNames = listOf<String>("apiKey")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/config/study",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        )?.wrap()
    }


    /**
     * 
     * list available push-notification services
     * @return kotlin.collections.List<PushNotificationServiceType>
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun listPushNotificationServices(): HttpResponse<List<PushNotificationServiceType>>? {

        val localVariableAuthNames = listOf<String>("apiKey")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/config/notifications",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        )?.wrap<ListPushNotificationServicesResponse>()?.map { value }
    }

    @Serializable
    private class ListPushNotificationServicesResponse(val value: List<PushNotificationServiceType>) {
        @Serializer(ListPushNotificationServicesResponse::class)
        companion object : KSerializer<ListPushNotificationServicesResponse> {
            private val serializer: KSerializer<List<PushNotificationServiceType>> = serializer<List<PushNotificationServiceType>>()
            override val descriptor = serializer.descriptor
            override fun serialize(encoder: Encoder, obj: ListPushNotificationServicesResponse) = serializer.serialize(encoder, obj.value)
            override fun deserialize(decoder: Decoder) = ListPushNotificationServicesResponse(
                serializer.deserialize(decoder))
        }
    }

    /**
     * 
     * store the client&#39;s push-notification token
     * @param serviceType 
     * @param pushNotificationToken  (optional)
     * @return void
     */
    open suspend fun setPushNotificationToken(serviceType: PushNotificationServiceType, pushNotificationToken: PushNotificationToken? = null): HttpResponse<Unit>? {

        val localVariableAuthNames = listOf<String>("apiKey")

        val localVariableBody = pushNotificationToken

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.PUT,
            "/config/notifications/{serviceType}".replace("{" + "serviceType" + "}", "$serviceType"),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

       var jsonRequest = jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        )
        return jsonRequest?.wrap()
    }
}
