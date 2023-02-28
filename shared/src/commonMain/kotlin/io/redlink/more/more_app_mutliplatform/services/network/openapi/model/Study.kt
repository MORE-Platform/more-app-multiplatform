@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package io.redlink.more.more_app_mutliplatform.services.network.openapi.model


import kotlinx.datetime.LocalDate
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * The study object containing all information and observation information to configure and initialize the APP
 *
 * @param studyTitle 
 * @param participantInfo 
 * @param consentInfo 
 * @param start 
 * @param end 
 * @param observations 
 * @param version A version indicator. Currently the last-modified date in EPOCH-format but that's not guaranteed.
 * @param active The current study-state. Mainly used during the registration process.
 */
@Serializable

data class Study (

    @SerialName(value = "studyTitle") @Required val studyTitle: kotlin.String,

    @SerialName(value = "participantInfo") @Required val participantInfo: kotlin.String,

    @SerialName(value = "consentInfo") @Required val consentInfo: kotlin.String,

    @SerialName(value = "start") @Required val start: LocalDate,

    @SerialName(value = "end") @Required val end: LocalDate,

    @SerialName(value = "observations") @Required val observations: kotlin.collections.List<Observation>,

    /* A version indicator. Currently the last-modified date in EPOCH-format but that's not guaranteed. */
    @SerialName(value = "version") @Required val version: kotlin.Long,

    /* The current study-state. Mainly used during the registration process. */
    @SerialName(value = "active") val active: kotlin.Boolean? = true

)
