/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */

package io.redlink.more.observations.longRunningObservation

import io.redlink.more.database.entities.AggregatedObservationDataEntity
import io.redlink.more.database.entities.ObservationDataEntity
import io.redlink.more.database.repository.MainRepository
import io.redlink.more.observations.appUsage.model.LogEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch

/**
 * Default implementation of LongRunningObservationStorage using AggregatedObservationDataRepository.
 */
class DefaultLongRunningObservationStorage(
    private val repos: MainRepository,
    private val observationId: String,
    private val observationType: String,
    private val toDataString: (Any) -> String
) : LongRunningObservationStorage {

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun <T> storeInstant(data: T, timestamp: Long) {
        scope.launch {
            if (data != null) {
                val observationDataEntity = ObservationDataEntity(
                    observationId = observationId,
                    observationType = observationType,
                    dataValue = toDataString(data),
                    timestamp = timestamp
                )
                repos.observationData.addData(listOf(observationDataEntity))
            }
        }
    }

    override fun <T> startObservation(data: T, identifier: String, timestamp: Long) {
        scope.launch {
            if (data != null) {
                val metadata =
                    if (data is LogEvent) data.aggregateKey(identifier ?: "") else identifier ?: ""
                val entity = AggregatedObservationDataEntity.fromData(
                    observationId = observationId,
                    observationType = observationType,
                    data = data,
                    startTimestamp = timestamp,
                    endTimestamp = timestamp,
                    metadata = metadata
                )
                repos.aggregatedObservationData.insert(entity)
            }
        }
    }

    private suspend fun <T> getOpenObservationEntity(
        data: T,
        identifier: String
    ): AggregatedObservationDataEntity? {
        val openEntities = repos.aggregatedObservationData.getByObservationId(observationId)
        if (data is LogEvent) {
            val key = data.aggregateKey(identifier)
            val exactMatch = openEntities.firstOrNull { it.metadata == key }
            if (exactMatch != null) return exactMatch

            if (data.family != null) {
                val familyPrefix = "${data.family.name}:"
                return openEntities.firstOrNull { it.metadata.startsWith(familyPrefix) }
            }
        }
        return if (openEntities.any { it.metadata == identifier }) {
            openEntities.first { it.metadata == identifier }
        } else {
            openEntities.firstOrNull()
        }
    }

    override fun <T> updateObservation(
        data: T,
        identifier: String,
        timestamp: Long
    ) {
        scope.launch {
            getOpenObservationEntity(data, identifier)?.let { entity ->
                if (data != null) {
                    repos.aggregatedObservationData.update(
                        entity.update(
                            data = data,
                            metadata = entity.metadata,
                            timestamp = timestamp
                        )
                    )
                }
            }
        }
    }

    override fun <T> inRangeObservation(data: T, identifier: String, timestamp: Long) {
        updateObservation(data, identifier, timestamp)
    }

    override fun <T> finishObservation(data: T, identifier: String, timestamp: Long) {
        scope.launch {
            getOpenObservationEntity(data, identifier)?.let { entity ->
                if (data != null) {
                    val updatedEntity = entity.update(
                        data = data,
                        metadata = entity.metadata,
                        timestamp = timestamp
                    )
                    repos.observationData.addData(listOf(updatedEntity.toObservationDataEntity<List<AggregatedObservationDataEntity.TimestampedData<T>>>()))
                    repos.aggregatedObservationData.delete(entity)
                }
            }
        }
    }
}
