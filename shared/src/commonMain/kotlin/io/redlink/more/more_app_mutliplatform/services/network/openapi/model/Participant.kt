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


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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

