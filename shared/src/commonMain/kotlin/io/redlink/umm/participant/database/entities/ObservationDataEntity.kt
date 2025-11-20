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
import io.redlink.umm.participant.extensions.asString
import io.redlink.umm.participant.observations.ObservationBulkModel
import io.redlink.umm.participant.services.network.openapi.model.ObservationData
import io.redlink.umm.participant.util.createUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

@Entity(tableName = "observation_data")
data class ObservationDataEntity(
    @PrimaryKey
    val dataId: String = createUUID(),
    val observationId: String = "",
    var observationType: String = "",
    val dataValue: String = "",
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
) {
    @Ignore
    fun timestampInstant() = Instant.fromEpochMilliseconds(timestamp)

    @Ignore
    fun asObservationData(): ObservationData =
        ObservationData(
            dataId = this.dataId,
            observationId = this.observationId,
            observationType = this.observationType,
            dataValue = try {
                Json.parseToJsonElement(dataValue).jsonObject
            } catch (e: Exception) {
                Napier.e(tag = this::class.asString()) { e.stackTraceToString() }
                JsonObject(emptyMap())
            },
            timestamp = timestampInstant()
        )

    override fun toString(): String {
        return "dataId: $dataId; observationId: $observationId; observationType: $observationType, timestamp: $timestamp, data: $dataValue;"
    }

    companion object {
        fun fromObservationData(observationData: ObservationData): ObservationDataEntity {
            return ObservationDataEntity(
                dataId = observationData.dataId,
                observationId = observationData.observationId,
                observationType = observationData.observationType,
                dataValue = observationData.dataValue?.let { Json.encodeToString(it) } ?: "",
                timestamp = observationData.timestamp.toEpochMilliseconds()
            )
        }

        fun fromData(data: Any, timestamp: Long = -1): ObservationDataEntity {
            val finalTimestamp = if (timestamp > 0) {
                timestamp * 1000 // Convert to milliseconds
            } else {
                Clock.System.now().toEpochMilliseconds()
            }

            return ObservationDataEntity(
                timestamp = finalTimestamp,
                dataValue = data.asString() ?: "{}"
            )
        }

        fun fromData(data: ObservationBulkModel): ObservationDataEntity {
            return fromData(data.data, data.timestamp)
        }

        fun fromData(data: Collection<ObservationBulkModel>): List<ObservationDataEntity> {
            return data.map { fromData(it) }
        }

        fun fromData(
            observationIdSet: Set<String>,
            data: Collection<ObservationBulkModel>
        ): List<ObservationDataEntity> {
            return observationIdSet.flatMap { id ->
                fromData(data).map { it.copy(observationId = id) }
            }
        }
    }
}
