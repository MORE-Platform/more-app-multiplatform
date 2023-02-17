@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.redlink.more.more_app_mutliplatform.services.network.openapi.model


import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * Credentials for the App for interacting with the backends 
 *
 * @param apiId 
 * @param apiKey 
 */
@Serializable

data class ApiKey (

    @SerialName(value = "apiId") @Required val apiId: kotlin.String,

    @SerialName(value = "apiKey") @Required val apiKey: kotlin.String

)

