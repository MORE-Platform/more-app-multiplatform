package io.redlink.umm.participant.database.dao

import androidx.room.Dao
import androidx.room.Query
import io.redlink.umm.participant.database.entities.DataPointEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DataPointDao : BaseDao<DataPointEntity> {
    @Query("SELECT COUNT(*) FROM data_points")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM data_points")
    fun getCountFlow(): Flow<Long>

    @Query("SELECT * FROM data_points WHERE scheduleId = :scheduleId LIMIT 1")
    fun getByScheduleId(scheduleId: String): Flow<DataPointEntity?>

    @Query("DELETE FROM data_points WHERE scheduleId = :scheduleId")
    suspend fun deleteByScheduleId(scheduleId: String)

    @Query("SELECT COUNT(*) FROM data_points WHERE scheduleId IN (:scheduleIds)")
    suspend fun getCountByScheduleIds(scheduleIds: Set<String>): Long

    @Query("DELETE FROM data_points")
    suspend fun deleteAll()

    @Query("SELECT * FROM data_points")
    suspend fun getAll(): List<DataPointEntity>
}