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
 * 
 *
 * @param observationId 
 * @param active 
 */
@Serializable

data class ObservationConsent (

    @SerialName(value = "observationId") @Required val observationId: kotlin.String,

    @SerialName(value = "active") @Required val active: kotlin.Boolean = true

)

