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
 * 
 *
 * @param projectId The Google Cloud project ID
 * @param applicationId The Google App ID that is used to uniquely identify an instance of an app.
 * @param apiKey 
 * @param databaseUrl 
 * @param gcmSenderId The Project Number from the Google Developer's console
 * @param storageBucket 
 */
@Serializable

data class FcmNotificationConfigAllOf (

    /* The Google Cloud project ID */
    @SerialName(value = "projectId") val projectId: kotlin.String? = null,

    /* The Google App ID that is used to uniquely identify an instance of an app. */
    @SerialName(value = "applicationId") val applicationId: kotlin.String? = null,

    @SerialName(value = "apiKey") val apiKey: kotlin.String? = null,

    @SerialName(value = "databaseUrl") val databaseUrl: kotlin.String? = null,

    /* The Project Number from the Google Developer's console */
    @SerialName(value = "gcmSenderId") val gcmSenderId: kotlin.String? = null,

    @SerialName(value = "storageBucket") val storageBucket: kotlin.String? = null

)

