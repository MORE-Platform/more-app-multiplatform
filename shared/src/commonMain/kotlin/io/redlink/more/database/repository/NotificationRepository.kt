/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.repository

import io.redlink.more.database.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    suspend fun storeNotification(notification: NotificationEntity)

    suspend fun storeNotifications(notifications: List<NotificationEntity>)

    suspend fun getNotification(notificationId: String): NotificationEntity?

    fun getAllUserFacingNotifications(): Flow<List<NotificationEntity>>

    suspend fun update(notificationId: String, read: Boolean? = null, priority: Long? = null)

    fun setNotificationReadStatus(key: String, read: Boolean = true)

    fun setNotificationCompletedStatus(key: String, completed: Boolean = true)

    suspend fun scheduledNotificationCount(): Int

    suspend fun scheduledNotifications(): List<NotificationEntity>

    fun deleteNotification(notificationId: String)

    suspend fun deleteAll()
}