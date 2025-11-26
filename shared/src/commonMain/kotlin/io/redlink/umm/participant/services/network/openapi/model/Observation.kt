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

package io.redlink.umm.participant.services.network.openapi.model

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * The configuration of an observation for the study.
 *
 * @param observationId
 * @param observationType
 * @param observationTitle
 * @param participantInfo
 * @param schedule
 * @param required
 * @param version A version indicator. Currently the last-modified date in EPOCH-format but that's not guaranteed.
 * @param configuration
 * @param hidden
 */
@Serializable

data class Observation(

    @SerialName(value = "observationId") @Required val observationId: String,

    @SerialName(value = "observationType") @Required val observationType: String,

    @SerialName(value = "observationTitle") @Required val observationTitle: String,

    @SerialName(value = "participantInfo") @Required val participantInfo: String,

    @SerialName(value = "schedule") @Required val schedule: List<ObservationSchedule>,

    @SerialName(value = "noSchedule") var noSchedule: Boolean = false,

    @SerialName(value = "required") @Required val required: Boolean = true,

    /* A version indicator. Currently the last-modified date in EPOCH-format but that's not guaranteed. */
    @SerialName(value = "version") @Required val version: Long,

    @SerialName(value = "configuration") val configuration: JsonObject? = null,

    @SerialName(value = "hidden") val hidden: Boolean? = false

)

