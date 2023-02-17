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
 * Confirms the participants consent to the study including supported observations on the device.
 *
 * @param consent Explicitly state the consent of the Participant
 * @param deviceId Identifier of the device used to provide consent
 * @param consentInfoMD5 MD5-Hash of the `consentInfo` (text) the participant actually gave consent. 
 * @param observations 
 */
@Serializable

data class StudyConsent (

    /* Explicitly state the consent of the Participant */
    @SerialName(value = "consent") @Required val consent: kotlin.Boolean = false,

    /* Identifier of the device used to provide consent */
    @SerialName(value = "deviceId") @Required val deviceId: kotlin.String,

    /* MD5-Hash of the `consentInfo` (text) the participant actually gave consent.  */
    @SerialName(value = "consentInfoMD5") @Required val consentInfoMD5: kotlin.String,

    @SerialName(value = "observations") @Required val observations: kotlin.collections.List<ObservationConsent>

)

