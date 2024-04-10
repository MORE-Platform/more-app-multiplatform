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

package io.redlink.more.more_app_mutliplatform.services.network.openapi.model

import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ApiKey

import kotlinx.serialization.*

/**
 * The configuration settings for the App while participating on a study 
 *
 * @param credentials 
 * @param endpoint base-uri of the App-API to use during the runtime of the study.  If omitted, the client should stay with the current endpoint. 
 */
@Serializable

data class AppConfiguration (

    @SerialName(value = "credentials") @Required val credentials: ApiKey,

    /* base-uri of the App-API to use during the runtime of the study.  If omitted, the client should stay with the current endpoint.  */
    @SerialName(value = "endpoint") val endpoint: kotlin.String? = null

)

