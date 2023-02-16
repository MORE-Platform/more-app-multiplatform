@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.redlink.more.more_app_mutliplatform.services.network.openapi.model

import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.ObservationData

import kotlinx.serialization.*

/**
 * A bulk of observation data containing a unique id, the API Key of the participant and the array of observation data
 *
 * @param bulkId 
 * @param dataPoints 
 */
@Serializable

data class DataBulk (

    @SerialName(value = "bulkId") @Required val bulkId: kotlin.String,

    @SerialName(value = "dataPoints") @Required val dataPoints: kotlin.collections.List<ObservationData>

)

