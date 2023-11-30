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
package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

class SimpleQuestionModel(
    var question: String = "",
    val answers: MutableSet<String> = mutableSetOf(""),
    var participantInfo: String = "",
    var observationId: String = "",
    var observationTitle: String = "",
    var scheduleId: String = ""
) {
    companion object {
        fun createModelFrom(observationSchema: ObservationSchema, scheduleId: String): SimpleQuestionModel {
            val config: Map<String, JsonElement> =
                observationSchema.configuration?.let { config ->
                    Json.decodeFromString<JsonObject>(config).toMap()
                } ?: emptyMap()
            return SimpleQuestionModel(
                config["question"]?.toString()?.trim('\"') ?: "",
                config["answers"]?.jsonArray?.map {
                    it.toString().trim('\"')
                }?.toMutableSet() ?: mutableSetOf(),
                observationSchema.participantInfo,
                observationSchema.observationId,
                observationSchema.observationTitle,
                scheduleId
            )

        }
    }
}