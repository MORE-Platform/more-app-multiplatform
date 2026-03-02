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
package io.redlink.more.models

import io.redlink.more.database.entities.ObservationEntity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray

class QuestionModel(
    var type: QuestionType,
    var question: String = "",
    val answers: List<String> = listOf(),
    var participantInfo: String = "",
    var observationId: String = "",
    var observationTitle: String = "",
    var scheduleId: String = ""
) {
    fun isValidModel() = type != QuestionType.NON

    companion object {
        fun createModelFrom(
            observationSchema: ObservationEntity,
            scheduleId: String
        ): QuestionModel {
            val config: Map<String, JsonElement> =
                observationSchema.configuration?.let { config ->
                    Json.decodeFromString<JsonObject>(config).toMap()
                } ?: emptyMap()
            val questionType =
                QuestionType.questionTypeForObservationType(observationSchema.observationType)
            return QuestionModel(
                questionType,
                config["question"]?.toString()?.trim('\"') ?: "",
                config["answers"]?.jsonArray?.map {
                    it.toString().trim('\"')
                } ?: emptyList(),
                observationSchema.participantInfo,
                observationSchema.observationId,
                observationSchema.observationTitle,
                scheduleId
            )
        }
    }
}