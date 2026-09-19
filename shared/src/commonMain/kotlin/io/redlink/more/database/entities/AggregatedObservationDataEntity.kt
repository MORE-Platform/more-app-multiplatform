/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.redlink.more.util.createUUID
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement

@Entity(tableName = "aggregated_observation_data")
data class AggregatedObservationDataEntity(
    @PrimaryKey val id: String = createUUID(),
    val observationId: String = "",
    val observationType: String = "",
    val startTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val endTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
    val data: String? = null,
    val metadata: String = ""
) {
    @Serializable
    data class TimestampedData<T>(
        val timestamp: Long,
        val data: T
    )

    @Ignore
    inline fun <reified T> getDataAs(): T? {
        return data?.let {
            Json.decodeFromJsonElement<T>(Json.parseToJsonElement(it))
        }
    }

    @Ignore
    inline fun <reified T> update(
        data: T,
        metadata: String = this.metadata,
        timestamp: Long = Clock.System.now().toEpochMilliseconds()
    ): AggregatedObservationDataEntity {
        val currentData: List<TimestampedData<T>> =
            getDataAs<List<TimestampedData<T>>>() ?: emptyList()
        val newData = currentData + TimestampedData(timestamp, data)
        return this.copy(
            data = Json.encodeToString(Json.encodeToJsonElement(newData)),
            metadata = metadata,
            endTimestamp = timestamp
        )
    }

    @Ignore
    inline fun <reified T> update(data: AggregatedObservationDataEntity): AggregatedObservationDataEntity? {
        return data.getDataAs<T>()?.let {
            return this.update<T>(it, data.metadata, data.endTimestamp)
        }
    }

    @Serializable
    data class PendingObservationPayload<T>(
        val startTimestamp: Long,
        val endTimestamp: Long,
        val metadata: String,
        val payload: T?
    )

    @Ignore
    inline fun <reified T> toObservationDataEntity(): ObservationDataEntity {
        return ObservationDataEntity(
            observationId = observationId,
            observationType = observationType,
            dataValue = Json.encodeToString(
                PendingObservationPayload(
                    startTimestamp = startTimestamp,
                    endTimestamp = endTimestamp,
                    metadata = metadata,
                    payload = getDataAs<T>()
                )
            ),
            timestamp = endTimestamp
        )
    }

    companion object {
        inline fun <reified T> fromData(
            observationId: String,
            observationType: String,
            data: T,
            startTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
            endTimestamp: Long = Clock.System.now().toEpochMilliseconds(),
            metadata: String = ""
        ): AggregatedObservationDataEntity {
            return AggregatedObservationDataEntity(
                observationId = observationId,
                observationType = observationType,
                data = Json.encodeToString(
                    Json.encodeToJsonElement(
                        listOf(
                            TimestampedData(
                                startTimestamp,
                                data
                            )
                        )
                    )
                ),
                startTimestamp = startTimestamp,
                endTimestamp = endTimestamp,
                metadata = metadata
            )
        }
    }
}