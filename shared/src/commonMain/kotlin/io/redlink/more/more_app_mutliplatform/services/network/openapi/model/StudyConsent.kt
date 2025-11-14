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


import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

