package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.ObservationSchema
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class SelfLearningMultipleChoiceQuestionModel (
    var question: String = "",
    val answers: MutableSet<String> = mutableSetOf(""),
    var participantInfo: String = "",
    var observationId: String = "",
    var observationTitle: String = "",
    var scheduleId: String = ""
) {
    companion object {
        fun createModelFrom(observationSchema: ObservationSchema, scheduleId: String, answers: MutableList<String>): SelfLearningMultipleChoiceQuestionModel {
            val config: Map<String, JsonElement> =
                observationSchema.configuration?.let { config ->
                    Json.decodeFromString<JsonObject>(config).toMap()
                } ?: emptyMap()
            return SelfLearningMultipleChoiceQuestionModel(
                config["question"]?.toString()?.trim('\"') ?: "",
                answers.map {
                    it.trim('\"')
                }.toMutableSet(),
                observationSchema.participantInfo,
                observationSchema.observationId,
                observationSchema.observationTitle,
                scheduleId
            )

        }
    }
}