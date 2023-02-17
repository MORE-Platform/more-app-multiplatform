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

