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
import io.redlink.more.database.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao : BaseDao<NotificationEntity> {

    @Query("DELETE FROM notifications WHERE notificationId = :notificationId")
    suspend fun deleteById(notificationId: String)

    @Query("DELETE FROM notifications WHERE channelId = :channelId")
    suspend fun deleteByChannelId(channelId: String)

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()

    @Query("SELECT * FROM notifications WHERE notificationId = :notificationId")
    suspend fun getById(notificationId: String): NotificationEntity?

    @Query("SELECT * FROM notifications WHERE notificationId = :notificationId")
    fun getByIdFlow(notificationId: String): Flow<NotificationEntity?>

    @Query("SELECT * FROM notifications")
    suspend fun getAll(): List<NotificationEntity>

    @Query("SELECT * FROM notifications")
    fun getAllFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE channelId = :channelId")
    suspend fun getByChannelId(channelId: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE channelId = :channelId")
    fun getByChannelIdFlow(channelId: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE read = :read")
    suspend fun getByReadStatus(read: Boolean): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE read = :read")
    fun getByReadStatusFlow(read: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE completed = :completed")
    suspend fun getByCompletedStatus(completed: Boolean): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE completed = :completed")
    fun getByCompletedStatusFlow(completed: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userFacing = :userFacing")
    suspend fun getByUserFacing(userFacing: Boolean): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userFacing = :userFacing")
    fun getByUserFacingFlow(userFacing: Boolean): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE priority = :priority")
    suspend fun getByPriority(priority: Long): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE priority = :priority")
    fun getByPriorityFlow(priority: Long): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE priority >= :minPriority")
    suspend fun getByMinPriority(minPriority: Long): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE priority >= :minPriority")
    fun getByMinPriorityFlow(minPriority: Long): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    suspend fun getByTimeRange(fromTimestamp: Long, toTimestamp: Long): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp")
    fun getByTimeRangeFlow(fromTimestamp: Long, toTimestamp: Long): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE deepLink IS NOT NULL AND deepLink != ''")
    suspend fun getWithDeepLink(): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE deepLink IS NOT NULL AND deepLink != ''")
    fun getWithDeepLinkFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :searchTerm || '%' OR notificationBody LIKE '%' || :searchTerm || '%'")
    suspend fun searchByContent(searchTerm: String): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE title LIKE '%' || :searchTerm || '%' OR notificationBody LIKE '%' || :searchTerm || '%'")
    fun searchByContentFlow(searchTerm: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatest(limit: Int): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userFacing = :userFacing")
    fun getByPastUserFacingFlow(
        userFacing: Boolean
    ): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE read = 0 AND userFacing = 1")
    fun getUnreadUserFacingFromPastFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM notifications WHERE timestamp > :currentTimestamp")
    suspend fun getScheduledNotificationCount(currentTimestamp: Long): Int

    @Query("SELECT * FROM notifications WHERE timestamp > :currentTimestamp")
    suspend fun getScheduledNotifications(currentTimestamp: Long): List<NotificationEntity>

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestFlow(limit: Int): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE userFacing = 1 ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestUserFacing(limit: Int): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE userFacing = 1 ORDER BY timestamp DESC LIMIT :limit")
    fun getLatestUserFacingFlow(limit: Int): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications WHERE read = 0 AND userFacing = 1 ORDER BY priority DESC, timestamp DESC")
    suspend fun getUnreadUserFacing(): List<NotificationEntity>

    @Query("SELECT * FROM notifications WHERE read = 0 AND userFacing = 1 ORDER BY priority DESC, timestamp DESC")
    fun getUnreadUserFacingFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notifications ORDER BY priority DESC, timestamp DESC")
    suspend fun getAllOrderedByPriorityAndTime(): List<NotificationEntity>

    @Query("SELECT * FROM notifications ORDER BY priority DESC, timestamp DESC")
    fun getAllOrderedByPriorityAndTimeFlow(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications")
    fun getCount(): Flow<Long>

    @Query("SELECT COUNT(*) FROM notifications WHERE read = :read")
    suspend fun getCountByReadStatus(read: Boolean): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE completed = :completed")
    suspend fun getCountByCompletedStatus(completed: Boolean): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE userFacing = :userFacing")
    fun getCountByUserFacing(userFacing: Boolean): Flow<Long>

    @Query("SELECT COUNT(*) FROM notifications WHERE read = 0 AND userFacing = 1")
    suspend fun getUnreadUserFacingCount(): Int

    @Query("SELECT COUNT(*) FROM notifications WHERE read = 0 AND userFacing = 1")
    fun getUnreadUserFacingCountFlow(): Flow<Int>

    @Query("SELECT DISTINCT channelId FROM notifications WHERE channelId IS NOT NULL")
    suspend fun getAllChannelIds(): List<String>

    @Query("UPDATE notifications SET read = :read WHERE notificationId = :notificationId")
    suspend fun updateReadStatus(notificationId: String, read: Boolean)

    @Query("UPDATE notifications SET completed = :completed WHERE notificationId = :notificationId")
    suspend fun updateCompletedStatus(notificationId: String, completed: Boolean)

    @Query("UPDATE notifications SET read = 1 WHERE channelId = :channelId")
    suspend fun markAllAsReadByChannelId(channelId: String)

    @Query("UPDATE notifications SET read = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications WHERE timestamp < :timestamp")
    suspend fun deleteOlderThan(timestamp: Long): Int

    @Query("DELETE FROM notifications WHERE read = 1 AND completed = 1 AND timestamp < :timestamp")
    suspend fun deleteOldReadAndCompleted(timestamp: Long): Int
}