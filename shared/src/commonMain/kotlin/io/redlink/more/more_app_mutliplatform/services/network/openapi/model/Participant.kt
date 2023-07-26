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
 * The study contact object containing all contact information for the study, that can be used when problems occure.
 *
 * @param alias States the alias name given on the study manager frontend.
 * @param id States the ID of the participant.
 */
@Serializable

data class Participant (

    @SerialName(value = "alias") val alias: kotlin.String? = null,

    @SerialName(value = "id") val id: kotlin.Int? = null,

)

