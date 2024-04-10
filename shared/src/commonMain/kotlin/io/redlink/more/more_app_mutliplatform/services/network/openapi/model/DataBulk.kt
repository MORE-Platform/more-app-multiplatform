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

