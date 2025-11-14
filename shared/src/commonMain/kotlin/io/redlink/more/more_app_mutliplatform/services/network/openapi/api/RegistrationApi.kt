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
import io.redlink.more.more_app_mutliplatform.services.network.openapi.infrastructure.wrap
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.AppConfiguration
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.StudyConsent
import kotlinx.serialization.json.Json

open class RegistrationApi(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonSerializer: Json = ApiClient.JSON_DEFAULT
) : ApiClient(baseUrl, httpClientEngine, httpClientConfig, jsonSerializer) {

    /**
     * 
     * Provide the information on a study required to register and consent.
     * @param moreRegistrationToken The token to register for a study
     * @return Study
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getStudyRegistrationInfo(moreRegistrationToken: kotlin.String): HttpResponse<Study>? {

        val localVariableAuthNames = listOf<String>()

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()
        moreRegistrationToken?.apply { localVariableHeaders["More-Registration-Token"] = this }

        val localVariableConfig = RequestConfig<Any?>(
            RequestMethod.GET,
            "/registration",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        )?.wrap()
    }


    /**
     * 
     * Perform the Registration to the Study and express the users consent.
     * @param moreRegistrationToken The token to register for a study
     * @param studyConsent 
     * @return AppConfiguration
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun registerForStudy(moreRegistrationToken: kotlin.String, studyConsent: StudyConsent): HttpResponse<AppConfiguration>? {

        val localVariableAuthNames = listOf<String>()

        val localVariableBody = studyConsent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()
        moreRegistrationToken?.apply { localVariableHeaders["More-Registration-Token"] = this.toString() }

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.POST,
            "/registration",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = false,
        )

        return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        )?.wrap()
    }



    /**
     * 
     * Leave study / Withdraw Consent
     * @return void
     */
    open suspend fun unregisterFromStudy(): HttpResponse<Unit>? {

        val localVariableAuthNames = listOf<String>("apiKey")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.DELETE,
            "/registration",
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
}
