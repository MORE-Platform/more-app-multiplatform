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
package io.redlink.umm.participant.database.repository

import io.ktor.utils.io.core.Closeable
import io.redlink.umm.participant.database.AppDatabase
import io.redlink.umm.participant.database.entities.ObservationEntity
import io.redlink.umm.participant.database.entities.ScheduleEntity
import io.redlink.umm.participant.extensions.asClosure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform
import kotlinx.datetime.Clock

class ObservationRepository(private val appDatabase: AppDatabase) {

    suspend fun getCount(): Int = appDatabase.observationDao().getCount()

    fun observations() = appDatabase.observationDao().getAllFlow()

    fun observationWithUndoneSchedules(): Flow<Map<ObservationEntity, List<ScheduleEntity>>> {
        return appDatabase.scheduleDao().getByDoneFlow(false)
            .combine(observations()) { schedules: List<ScheduleEntity>, observations: List<ObservationEntity> ->
                observations.associateWith { observation ->
                    schedules.filter { schedule -> schedule.observationId == observation.observationId }
                }
            }
    }

    suspend fun updateLastCollection(type: String, timestamp: Long) {
        val observations = appDatabase.observationDao().getByObservationType(type)
        observations.forEach { observation ->
            val updatedObservation = observation.copy(collectionTimestamp = timestamp)
            appDatabase.observationDao().update(updatedObservation)
        }
    }

    suspend fun updateLastCollection(types: Set<String>, timestamp: Long) {
        types.forEach { type ->
            val observations = appDatabase.observationDao().getByObservationType(type)
            observations.forEach { observation ->
                val updatedObservation = observation.copy(collectionTimestamp = timestamp)
                appDatabase.observationDao().update(updatedObservation)
            }
        }
    }

    fun collectionTimestamp(type: String): Flow<Long?> =
        appDatabase.observationDao().getByObservationTypeFlow(type)
            .transform { observationList ->
                emit(observationList.firstOrNull()?.collectionTimestamp)
            }

    fun collectAllTimestamps(): Flow<Map<String, Long>> =
        observations().transform { observationList ->
            emit(observationList.associate { it.observationType to it.collectionTimestamp })
        }

    fun collectTimestampForObservationIds(observationIds: Set<String>): Flow<Long> =
        observations().transform { observationList ->
            val filteredObservations =
                observationList.filter { it.observationType in observationIds }
            val maxTimestamp =
                filteredObservations.maxByOrNull { it.collectionTimestamp }?.collectionTimestamp
                    ?: Clock.System.now().toEpochMilliseconds()
            emit(maxTimestamp)
        }

    fun collectTimestampOfType(type: String, newState: (Long?) -> Unit): Closeable {
        return collectionTimestamp(type).asClosure(newState)
    }

    fun collectAllTimestamps(newState: (Map<String, Long>) -> Unit): Closeable {
        return collectAllTimestamps().asClosure(newState)
    }

    fun collectObservationsWithUndoneSchedules(newState: (Map<ObservationEntity, List<ScheduleEntity>>) -> Unit): Closeable {
        return observationWithUndoneSchedules().asClosure(newState)
    }

    fun observationTypes(): Flow<Set<String>> = observations().transform { observationList ->
        emit(observationList.map { it.observationType }.toSet())
    }

    fun observationById(observationId: String) =
        appDatabase.observationDao().getByObservationIdFlow(observationId)

    suspend fun getObservationByObservationId(observationId: String): ObservationEntity? {
        return appDatabase.observationDao().getByObservationId(observationId)
    }
}