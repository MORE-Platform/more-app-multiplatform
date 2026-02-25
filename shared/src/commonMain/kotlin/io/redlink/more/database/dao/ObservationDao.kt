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
package io.redlink.more.database.dao

import androidx.room.Dao
import androidx.room.Query
import io.redlink.more.database.entities.ObservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObservationDao : BaseDao<ObservationEntity> {

    @Query("DELETE FROM observations WHERE observationId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM observations WHERE observationId = :observationId")
    suspend fun deleteByObservationId(observationId: String)

    @Query("DELETE FROM observations")
    suspend fun deleteAll()

    @Query("SELECT * FROM observations WHERE observationId = :observationId")
    suspend fun getByObservationId(observationId: String): ObservationEntity?

    @Query("SELECT * FROM observations WHERE observationId = :observationId")
    fun getByObservationIdFlow(observationId: String): Flow<ObservationEntity?>

    @Query("SELECT * FROM observations")
    suspend fun getAll(): List<ObservationEntity>

    @Query("SELECT * FROM observations")
    fun getAllFlow(): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE observationType = :observationType")
    suspend fun getByObservationType(observationType: String): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE observationType = :observationType")
    fun getByObservationTypeFlow(observationType: String): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE hidden = :hidden")
    suspend fun getByHidden(hidden: Boolean): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE hidden = :hidden")
    fun getByHiddenFlow(hidden: Boolean): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE scheduleLess = :scheduleLess")
    suspend fun getByScheduleLess(scheduleLess: Boolean): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE scheduleLess = :scheduleLess")
    fun getByScheduleLessFlow(scheduleLess: Boolean): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE required = :required")
    suspend fun getByRequired(required: Boolean): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE required = :required")
    fun getByRequiredFlow(required: Boolean): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE version = :version")
    suspend fun getByVersion(version: Long): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE collectionTimestamp >= :fromTimestamp AND collectionTimestamp <= :toTimestamp")
    suspend fun getByTimeRange(fromTimestamp: Long, toTimestamp: Long): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE collectionTimestamp >= :fromTimestamp AND collectionTimestamp <= :toTimestamp")
    fun getByTimeRangeFlow(fromTimestamp: Long, toTimestamp: Long): Flow<List<ObservationEntity>>

    @Query("SELECT * FROM observations WHERE observationTitle LIKE '%' || :searchTerm || '%'")
    suspend fun searchByTitle(searchTerm: String): List<ObservationEntity>

    @Query("SELECT * FROM observations WHERE participantInfo LIKE '%' || :searchTerm || '%'")
    suspend fun searchByParticipantInfo(searchTerm: String): List<ObservationEntity>

    @Query("SELECT COUNT(*) FROM observations")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM observations WHERE observationType = :observationType")
    suspend fun getCountByType(observationType: String): Int

    @Query("SELECT COUNT(*) FROM observations WHERE required = :required")
    suspend fun getCountByRequired(required: Boolean): Int

    @Query("SELECT DISTINCT observationType FROM observations")
    suspend fun getAllObservationTypes(): List<String>

    @Query("UPDATE observations SET version = :version WHERE observationId = :observationId")
    suspend fun updateVersion(observationId: String, version: Long)

    @Query("UPDATE observations SET hidden = :hidden WHERE observationId = :observationId")
    suspend fun updateHidden(observationId: String, hidden: Boolean)
}