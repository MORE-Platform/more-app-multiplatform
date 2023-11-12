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

