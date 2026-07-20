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
import io.redlink.more.database.entities.ScheduleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao : BaseDao<ScheduleEntity> {

    @Query("DELETE FROM schedules WHERE scheduleId = :scheduleId")
    suspend fun deleteById(scheduleId: String)

    @Query("DELETE FROM schedules WHERE observationId = :observationId")
    suspend fun deleteByObservationId(observationId: String)

    @Query("DELETE FROM schedules")
    suspend fun deleteAll()

    @Query("SELECT * FROM schedules WHERE scheduleId = :scheduleId")
    fun getById(scheduleId: String): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedules WHERE scheduleId = :scheduleId")
    fun getByIdFlow(scheduleId: String): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedules")
    suspend fun getAll(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules")
    fun getAllFlow(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE observationId = :observationId")
    suspend fun getByObservationId(observationId: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE observationId = :observationId")
    fun getByObservationIdFlow(observationId: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE observationType = :observationType")
    suspend fun getByObservationType(observationType: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE observationType = :observationType")
    fun getByObservationTypeFlow(observationType: String): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE done = :done")
    suspend fun getByDone(done: Boolean): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE done = :done")
    fun getByDoneFlow(done: Boolean): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE state IN (:states)")
    fun getByStatesFlow(states: List<String>): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE hidden = :hidden")
    suspend fun getByHidden(hidden: Boolean): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE hidden = :hidden")
    fun getByHiddenFlow(hidden: Boolean): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE state = :state")
    suspend fun getByState(state: String): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE state = :state")
    fun getByStateFlow(state: String): Flow<List<ScheduleEntity>>

    @Query(
        "SELECT * " +
                "FROM schedules " +
                "WHERE state IN (:states) " +
                "AND reminder = 1" +
                " AND start >= :minTimestamp" +
                " AND start <= :maxTimestamp " +
                "ORDER BY start ASC" +
                " LIMIT :limit"
    )
    fun getSchedulesWithReminder(
        states: List<String>,
        minTimestamp: Long,
        maxTimestamp: Long,
        limit: Int
    ): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE start <= :timestamp AND `end` >= :timestamp")
    suspend fun getActiveSchedulesAtTime(timestamp: Long): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE start <= :currentTime AND done = 0 AND hidden = 0")
    suspend fun getAvailableSchedules(currentTime: Long): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE start <= :currentTime AND done = 0 AND hidden = 0")
    fun getAvailableSchedulesFlow(currentTime: Long): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules WHERE `end` < :timestamp AND done = 0")
    suspend fun getExpiredSchedules(timestamp: Long): List<ScheduleEntity>

    @Query("SELECT COUNT(*) FROM schedules")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM schedules")
    fun countAsFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM schedules WHERE done = :done")
    suspend fun getCountByDone(done: Boolean): Int

    @Query("SELECT COUNT(*) FROM schedules WHERE observationId = :observationId")
    suspend fun getCountByObservationId(observationId: String): Int

    @Query("SELECT DISTINCT observationType FROM schedules WHERE scheduleId IN (:scheduleIds)")
    fun getObservationTypesForScheduleIds(scheduleIds: Set<String>): Flow<List<String>>

    @Query("UPDATE schedules SET done = :done WHERE scheduleId = :scheduleId")
    suspend fun updateDoneStatus(scheduleId: String, done: Boolean)

    @Query("UPDATE schedules SET state = :state WHERE scheduleId = :scheduleId")
    suspend fun updateState(scheduleId: String, state: String)
}