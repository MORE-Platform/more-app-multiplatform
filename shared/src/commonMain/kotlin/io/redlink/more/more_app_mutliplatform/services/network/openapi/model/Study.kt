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


import kotlinx.datetime.LocalDate
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The study object containing all information and observation information to configure and initialize the APP
 *
 * @param studyTitle 
 * @param participantInfo 
 * @param consentInfo 
 * @param start 
 * @param end 
 * @param observations
 * @param contact
 * @param version A version indicator. Currently the last-modified date in EPOCH-format but that's not guaranteed.
 * @param active The current study-state. Mainly used during the registration process.
 * @param finishText Finish message, when the study is set to completed.
 */
@Serializable

data class Study(

    @SerialName(value = "studyTitle") @Required val studyTitle: String,

    @SerialName(value = "participantInfo") @Required val participantInfo: String,

    @SerialName(value = "participant") val participant: Participant? = Participant(),

    @SerialName(value = "consentInfo") @Required val consentInfo: String,

    @SerialName(value = "start") @Required val start: LocalDate,

    @SerialName(value = "end") @Required val end: LocalDate,

    @SerialName(value = "observations") @Required val observations: List<Observation>,

    @SerialName(value = "contact") val contact: StudyContact? = StudyContact(),

    /* A version indicator. Currently the last-modified date in EPOCH-format but that's not guaranteed. */
    @SerialName(value = "version") @Required val version: Long,

    /* The current study-state. Mainly used during the registration process. */
    @SerialName(value = "active") val active: Boolean? = true,

    @SerialName(value = "studyState") val studyState: String? = if (active == true) "active" else "passive",

    @SerialName(value = "finishText") val finishText: String? = null
)

