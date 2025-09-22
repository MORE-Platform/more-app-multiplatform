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
package io.redlink.more.more_app_mutliplatform.database.dao

import androidx.room.Dao
import androidx.room.Query
import io.redlink.more.more_app_mutliplatform.database.entities.ObservationDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDataDao : BaseDao<ObservationDataEntity> {

    @Query("DELETE FROM observation_data WHERE dataId = :dataId")
    suspend fun deleteById(dataId: String)

    @Query("DELETE FROM observation_data WHERE observationId = :observationId")
    suspend fun deleteByObservationId(observationId: String)

    @Query("DELETE FROM observation_data WHERE observationType = :observationType")
    suspend fun deleteByObservationType(observationType: String)

    @Query("DELETE FROM observation_data")
    suspend fun deleteAll()

    @Query("SELECT * FROM observation_data WHERE dataId = :dataId")
    suspend fun getById(dataId: String): ObservationDataEntity?

    @Query("SELECT * FROM observation_data WHERE dataId = :dataId")
    fun getByIdFlow(dataId: String): Flow<ObservationDataEntity?>

    @Query("SELECT * FROM observation_data")
    suspend fun getAll(): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data")
    fun getAllFlow(): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE observationId = :observationId")
    suspend fun getByObservationId(observationId: String): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE observationId = :observationId")
    fun getByObservationIdFlow(observationId: String): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE observationType = :observationType")
    suspend fun getByObservationType(observationType: String): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE observationType = :observationType")
    fun getByObservationTypeFlow(observationType: String): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE observationId = :observationId AND observationType = :observationType")
    suspend fun getByObservationIdAndType(
        observationId: String,
        observationType: String
    ): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE observationId = :observationId AND observationType = :observationType")
    fun getByObservationIdAndTypeFlow(
        observationId: String,
        observationType: String
    ): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    suspend fun getByTimeRange(fromTimestamp: Long, toTimestamp: Long): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    fun getByTimeRangeFlow(
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE observationId = :observationId AND timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    suspend fun getByObservationIdAndTimeRange(
        observationId: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE observationId = :observationId AND timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    fun getByObservationIdAndTimeRangeFlow(
        observationId: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE observationType = :observationType AND timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    suspend fun getByObservationTypeAndTimeRange(
        observationType: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE observationType = :observationType AND timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    fun getByObservationTypeAndTimeRangeFlow(
        observationType: String,
        fromTimestamp: Long,
        toTimestamp: Long
    ): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE timestamp >= :timestamp ORDER BY timestamp ASC")
    suspend fun getFromTimestamp(timestamp: Long): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE timestamp >= :timestamp ORDER BY timestamp ASC")
    fun getFromTimestampFlow(timestamp: Long): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE timestamp <= :timestamp ORDER BY timestamp DESC")
    suspend fun getUpToTimestamp(timestamp: Long): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE timestamp <= :timestamp ORDER BY timestamp DESC")
    fun getUpToTimestampFlow(timestamp: Long): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestFlow(limit: Int): Flow<List<ObservationDataEntity>>

    @Query("SELECT * FROM observation_data WHERE observationId = :observationId ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestByObservationId(
        observationId: String,
        limit: Int
    ): List<ObservationDataEntity>

    @Query("SELECT * FROM observation_data WHERE observationId = :observationId ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestByObservationIdFlow(
        observationId: String,
        limit: Int
    ): Flow<List<ObservationDataEntity>>

    @Query("SELECT COUNT(*) FROM observation_data")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM observation_data WHERE observationId = :observationId")
    suspend fun getCountByObservationId(observationId: String): Int

    @Query("SELECT COUNT(*) FROM observation_data WHERE observationType = :observationType")
    suspend fun getCountByObservationType(observationType: String): Int

    @Query("SELECT COUNT(*) FROM observation_data WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    suspend fun getCountByTimeRange(fromTimestamp: Long, toTimestamp: Long): Int

    @Query("SELECT DISTINCT observationType FROM observation_data")
    suspend fun getAllObservationTypes(): List<String>

    @Query("SELECT DISTINCT observationId FROM observation_data")
    suspend fun getAllObservationIds(): List<String>

    @Query("SELECT MIN(timestamp) FROM observation_data")
    suspend fun getEarliestTimestamp(): Long?

    @Query("SELECT MAX(timestamp) FROM observation_data")
    suspend fun getLatestTimestamp(): Long?

    @Query("DELETE FROM observation_data WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    @Query("DELETE FROM observation_data WHERE observationId = :observationId AND timestamp < :timestamp")
    suspend fun deleteOlderThanByObservationId(observationId: String, timestamp: Long): Int
}