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

import io.redlink.more.database.entities.AggregatedObservationDataEntity
import kotlinx.coroutines.flow.Flow

interface AggregatedObservationDataRepository {
    suspend fun insert(entity: AggregatedObservationDataEntity)
    suspend fun insertAll(entities: List<AggregatedObservationDataEntity>)
    suspend fun update(entity: AggregatedObservationDataEntity)
    suspend fun delete(entity: AggregatedObservationDataEntity)
    suspend fun deleteById(id: String)
    suspend fun deleteByObservationId(observationId: String)
    suspend fun deleteAll()

    suspend fun getById(id: String): AggregatedObservationDataEntity?
    fun getByIdFlow(id: String): Flow<AggregatedObservationDataEntity?>

    suspend fun getByObservationId(observationId: String): List<AggregatedObservationDataEntity>
    fun getByObservationIdFlow(observationId: String): Flow<List<AggregatedObservationDataEntity>>

    suspend fun getByObservationType(observationType: String): List<AggregatedObservationDataEntity>
    fun getByObservationTypeFlow(observationType: String): Flow<List<AggregatedObservationDataEntity>>

    suspend fun getAll(): List<AggregatedObservationDataEntity>
    fun getAllFlow(): Flow<List<AggregatedObservationDataEntity>>

    suspend fun getCount(): Int
    suspend fun getCountByObservationId(observationId: String): Int
}
