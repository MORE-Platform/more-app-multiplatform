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
package io.redlink.more.database.repository

import io.ktor.utils.io.core.Closeable
import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.LatestObservationDataEntity
import io.redlink.more.database.entities.ObservationEntity
import io.redlink.more.database.entities.ScheduleEntity
import io.redlink.more.extensions.asClosure
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.transform
import kotlinx.datetime.Clock

class ObservationRepositoryImpl(private val appDatabase: AppDatabase) : ObservationRepository {

    override suspend fun getCount(): Int = appDatabase.observationDao().getCount()

    override fun observations(): Flow<List<ObservationEntity>> =
        appDatabase.observationDao().getAllFlow()

    override fun observationWithUndoneSchedules(): Flow<Map<ObservationEntity, List<ScheduleEntity>>> {
        return appDatabase.scheduleDao().getAllFlow()
            .combine(observations()) { schedules: List<ScheduleEntity>, observations: List<ObservationEntity> ->
                observations.associateWith { observation ->
                    schedules.filter { schedule ->
                        schedule.observationId == observation.observationId && !schedule.getState()
                            .completed()
                    }
                }
            }
    }

    override suspend fun updateLastCollection(type: String, timestamp: Long) {
        updateLastCollection(setOf(type), timestamp)
    }

    override suspend fun updateLastCollection(types: Set<String>, timestamp: Long) {
        types.forEach { type ->
            val observations = appDatabase.observationDao().getByObservationType(type)
            observations.forEach { observation ->
                if (observation.collectionTimestamp != timestamp) {
                    val updatedObservation = observation.copy(collectionTimestamp = timestamp)
                    appDatabase.observationDao().update(updatedObservation)
                }
            }
        }
    }

    override fun collectionTimestamp(type: String): Flow<Long?> =
        appDatabase.observationDao().getByObservationTypeFlow(type)
            .transform { observationList ->
                emit(observationList.firstOrNull()?.collectionTimestamp)
            }

    override fun collectAllTimestamps(): Flow<Map<String, Long>> =
        observations().transform { observationList ->
            emit(observationList.associate { it.observationType to it.collectionTimestamp })
        }

    override fun collectTimestampForObservationIds(observationIds: Set<String>): Flow<Long> =
        observations().transform { observationList ->
            val filteredObservations =
                observationList.filter { it.observationType in observationIds }
            val maxTimestamp =
                filteredObservations.maxByOrNull { it.collectionTimestamp }?.collectionTimestamp
                    ?: Clock.System.now().toEpochMilliseconds()
            emit(maxTimestamp)
        }

    override fun collectTimestampOfType(type: String, newState: (Long?) -> Unit): Closeable =
        collectionTimestamp(type).asClosure(newState)

    override fun collectAllTimestamps(newState: (Map<String, Long>) -> Unit): Closeable {
        return collectAllTimestamps().asClosure(newState)
    }

    override fun collectObservationsWithUndoneSchedules(newState: (Map<ObservationEntity, List<ScheduleEntity>>) -> Unit): Closeable =
        observationWithUndoneSchedules().asClosure(newState)

    override fun observationTypes(): Flow<Set<String>> =
        observations().transform { observationList ->
            emit(observationList.map { it.observationType }.toSet())
        }

    override fun observationById(observationId: String): Flow<ObservationEntity?> =
        appDatabase.observationDao().getByObservationIdFlow(observationId)

    override suspend fun getObservationByObservationId(observationId: String): ObservationEntity? {
        return appDatabase.observationDao().getByObservationId(observationId)
    }

    override suspend fun storeLatestDataPoint(data: LatestObservationDataEntity) {
        appDatabase.latestObservationDataDao().upsert(data)
    }

    override fun latestDataPointForSchedule(scheduleId: String): Flow<LatestObservationDataEntity?> =
        appDatabase.latestObservationDataDao().getByScheduleId(scheduleId)

    override suspend fun latestDataPointTimestamp(observationType: String): Long? =
        appDatabase.latestObservationDataDao()
            .getLatestByObservationType(observationType)?.timestamp
}
