/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.dao

import androidx.room.Dao
import androidx.room.Query
import io.redlink.more.database.entities.AggregatedObservationDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AggregatedObservationDataDao : BaseDao<AggregatedObservationDataEntity> {

    @Query("SELECT * FROM aggregated_observation_data WHERE id = :id")
    suspend fun getById(id: String): AggregatedObservationDataEntity?

    @Query("SELECT * FROM aggregated_observation_data WHERE id = :id")
    fun getByIdFlow(id: String): Flow<AggregatedObservationDataEntity?>

    @Query("SELECT * FROM aggregated_observation_data WHERE observationId = :observationId")
    suspend fun getByObservationId(observationId: String): List<AggregatedObservationDataEntity>

    @Query("SELECT * FROM aggregated_observation_data WHERE observationId = :observationId")
    fun getByObservationIdFlow(observationId: String): Flow<List<AggregatedObservationDataEntity>>

    @Query("SELECT * FROM aggregated_observation_data WHERE observationType = :observationType")
    suspend fun getByObservationType(observationType: String): List<AggregatedObservationDataEntity>

    @Query("SELECT * FROM aggregated_observation_data WHERE observationType = :observationType")
    fun getByObservationTypeFlow(observationType: String): Flow<List<AggregatedObservationDataEntity>>

    @Query("SELECT * FROM aggregated_observation_data")
    suspend fun getAll(): List<AggregatedObservationDataEntity>

    @Query("SELECT * FROM aggregated_observation_data")
    fun getAllFlow(): Flow<List<AggregatedObservationDataEntity>>

    @Query("DELETE FROM aggregated_observation_data WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM aggregated_observation_data WHERE observationId = :observationId")
    suspend fun deleteByObservationId(observationId: String)

    @Query("DELETE FROM aggregated_observation_data")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM aggregated_observation_data")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM aggregated_observation_data WHERE observationId = :observationId")
    suspend fun getCountByObservationId(observationId: String): Int
}
