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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.redlink.more.database.entities.LatestObservationDataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LatestObservationDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(data: LatestObservationDataEntity)

    @Query("SELECT * FROM latest_observation_data WHERE scheduleId = :scheduleId")
    fun getByScheduleId(scheduleId: String): Flow<LatestObservationDataEntity?>

    @Query("SELECT * FROM latest_observation_data WHERE observationType = :observationType ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestByObservationType(observationType: String): LatestObservationDataEntity?

    @Query("DELETE FROM latest_observation_data WHERE scheduleId = :scheduleId")
    suspend fun deleteByScheduleId(scheduleId: String)

    @Query("DELETE FROM latest_observation_data")
    suspend fun deleteAll()
}
