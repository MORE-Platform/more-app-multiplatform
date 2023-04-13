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
    var observationTitle: String = ""
) {
    companion object {
        fun createModelFrom(observationSchema: ObservationSchema): SimpleQuestionModel {
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
                observationSchema.observationTitle
            )

        }
    }
}