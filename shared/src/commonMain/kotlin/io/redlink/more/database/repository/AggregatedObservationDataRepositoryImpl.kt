/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.repository

import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.AggregatedObservationDataEntity
import kotlinx.coroutines.flow.Flow

class AggregatedObservationDataRepositoryImpl(private val appDatabase: AppDatabase) :
    AggregatedObservationDataRepository {

    override suspend fun insert(entity: AggregatedObservationDataEntity) =
        appDatabase.aggregatedObservationDataDao().insert(entity)

    override suspend fun insertAll(entities: List<AggregatedObservationDataEntity>) =
        appDatabase.aggregatedObservationDataDao().insertAll(entities)

    override suspend fun update(entity: AggregatedObservationDataEntity) =
        appDatabase.aggregatedObservationDataDao().update(entity)

    override suspend fun delete(entity: AggregatedObservationDataEntity) =
        appDatabase.aggregatedObservationDataDao().delete(entity)

    override suspend fun deleteById(id: String) =
        appDatabase.aggregatedObservationDataDao().deleteById(id)

    override suspend fun deleteByObservationId(observationId: String) =
        appDatabase.aggregatedObservationDataDao().deleteByObservationId(observationId)

    override suspend fun deleteAll() =
        appDatabase.aggregatedObservationDataDao().deleteAll()

    override suspend fun getById(id: String): AggregatedObservationDataEntity? =
        appDatabase.aggregatedObservationDataDao().getById(id)

    override fun getByIdFlow(id: String): Flow<AggregatedObservationDataEntity?> =
        appDatabase.aggregatedObservationDataDao().getByIdFlow(id)

    override suspend fun getByObservationId(observationId: String): List<AggregatedObservationDataEntity> =
        appDatabase.aggregatedObservationDataDao().getByObservationId(observationId)

    override fun getByObservationIdFlow(observationId: String): Flow<List<AggregatedObservationDataEntity>> =
        appDatabase.aggregatedObservationDataDao().getByObservationIdFlow(observationId)

    override suspend fun getByObservationType(observationType: String): List<AggregatedObservationDataEntity> =
        appDatabase.aggregatedObservationDataDao().getByObservationType(observationType)

    override fun getByObservationTypeFlow(observationType: String): Flow<List<AggregatedObservationDataEntity>> =
        appDatabase.aggregatedObservationDataDao().getByObservationTypeFlow(observationType)

    override suspend fun getAll(): List<AggregatedObservationDataEntity> =
        appDatabase.aggregatedObservationDataDao().getAll()

    override fun getAllFlow(): Flow<List<AggregatedObservationDataEntity>> =
        appDatabase.aggregatedObservationDataDao().getAllFlow()

    override suspend fun getCount(): Int =
        appDatabase.aggregatedObservationDataDao().getCount()

    override suspend fun getCountByObservationId(observationId: String): Int =
        appDatabase.aggregatedObservationDataDao().getCountByObservationId(observationId)
}
