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
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.DataBulk
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

open class DataApi(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonSerializer: Json = ApiClient.JSON_DEFAULT
) : ApiClient(baseUrl, httpClientEngine, httpClientConfig, jsonSerializer) {

    /**
     *
     * add data to elastic shard
     * @param dataBulk  (optional)
     * @return kotlin.collections.List<kotlin.String>
     */
    open suspend fun storeBulk(dataBulk: DataBulk? = null): HttpResponse<List<String>>? {

        val localVariableAuthNames = listOf("apiKey")

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<Any?>(
            RequestMethod.POST,
            "/data/bulk",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return jsonRequest(
            localVariableConfig,
            dataBulk,
            localVariableAuthNames
        )?.wrap<StoreBulkResponse>()?.map { value }
    }


    @Serializable
    private class StoreBulkResponse(val value: List<kotlin.String>) {
        @OptIn(ExperimentalSerializationApi::class)
        @Serializer(StoreBulkResponse::class)
        companion object : KSerializer<StoreBulkResponse> {
            private val serializer: KSerializer<List<kotlin.String>> =
                serializer<List<kotlin.String>>()
            override val descriptor = serializer.descriptor
            override fun serialize(encoder: Encoder, obj: StoreBulkResponse) =
                serializer.serialize(encoder, obj.value)

            override fun deserialize(decoder: Decoder) =
                StoreBulkResponse(serializer.deserialize(decoder))
        }
    }

}
