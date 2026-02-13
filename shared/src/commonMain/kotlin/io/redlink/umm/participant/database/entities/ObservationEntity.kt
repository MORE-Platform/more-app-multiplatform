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
package io.redlink.umm.participant.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.github.aakira.napier.Napier
import io.redlink.umm.blendedcare.services.network.openapi.model.Observation
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

@Entity(tableName = "observations")
data class ObservationEntity(
    @PrimaryKey
    val observationId: String = "",
    val observationType: String = "",
    val observationTitle: String = "",
    val participantInfo: String = "",
    val configuration: String? = null,
    val hidden: Boolean? = null,
    val scheduleLess: Boolean = false,
    val reminder: Boolean = false,
    val version: Long = 0,
    val required: Boolean = false,
    val collectionTimestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    @Ignore
    fun collectionTimestampInstant() = Instant.fromEpochMilliseconds(collectionTimestamp)

    @Ignore
    fun configAsMap(): Map<String, Any> = configuration?.let { config ->
        try {
            Json.decodeFromString<JsonObject>(config).toMap()
        } catch (e: Exception) {
            Napier.e { e.stackTraceToString() }
            emptyMap()
        }
    } ?: emptyMap()

    companion object {
        fun toEntity(observation: Observation): ObservationEntity {
            return ObservationEntity(
                observationId = observation.observationId,
                observationTitle = observation.observationTitle,
                observationType = observation.observationType,
                participantInfo = observation.participantInfo,
                configuration = observation.configuration.toString(),
                hidden = observation.hidden,
                scheduleLess = observation.noSchedule ?: false,
                reminder = observation.reminder ?: false,
                required = observation.required,
                version = observation.version
            )
        }
    }
}
