@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.redlink.more.more_app_mutliplatform.services.network.openapi.model

import kotlinx.datetime.Instant
import kotlinx.serialization.*
import kotlinx.serialization.json.JsonObject

/**
 * 
 *
 * @param dataId 
 * @param observationId 
 * @param observationType 
 * @param dataValue 
 * @param timestamp 
 */
@Serializable

data class ObservationData (

    @SerialName(value = "dataId") @Required val dataId: kotlin.String,

    @SerialName(value = "observationId") @Required val observationId: kotlin.String,

    @SerialName(value = "observationType") @Required val observationType: kotlin.String,

    @SerialName(value = "dataValue") @Required val dataValue: JsonObject? = null,

    @SerialName(value = "timestamp") @Required val timestamp: Instant

)

