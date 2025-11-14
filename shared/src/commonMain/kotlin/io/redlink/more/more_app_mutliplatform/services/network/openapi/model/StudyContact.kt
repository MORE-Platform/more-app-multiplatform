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
 * @param institute States the institute, that handles the study.
 * @param person States the person a participant can contact, when problems occure. Is required.
 * @param email States the contact email address, that can be written to. Is required.
 * @param phoneNumber States the contact phone number to contact, if added.
 */
@Serializable

data class StudyContact (

    @SerialName(value = "institute") val institute: kotlin.String? = null,

    @SerialName(value = "person") val person: kotlin.String? = null,

    @SerialName(value = "email") val email: kotlin.String? = null,

    @SerialName(value = "phoneNumber") val phoneNumber: kotlin.String? = null,

)

