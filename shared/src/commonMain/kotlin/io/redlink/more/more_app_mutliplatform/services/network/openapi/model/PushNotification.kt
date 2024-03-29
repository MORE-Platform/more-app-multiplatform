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
package io.redlink.more.more_app_mutliplatform.services.network.openapi.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class PushNotification(
    @SerialName(value = "type") @Required val type: String,
    @SerialName(value = "msgId") @Required val msgId: String,
    @SerialName(value = "title") @Required val title: String? = null,
    @SerialName(value = "body") @Required val body: String? = null,
    @SerialName(value = "data") @Required val data: JsonObject? = null,
    @SerialName(value = "deepLink") val deepLink: String? = null,
    @SerialName(value = "timestamp") @Required val timestamp: Instant? = null
)