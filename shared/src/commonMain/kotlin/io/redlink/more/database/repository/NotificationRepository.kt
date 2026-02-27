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

import io.github.aakira.napier.Napier
import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.scopes.Scope
import io.redlink.more.util.alignedNowFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class NotificationRepository(private val appDatabase: AppDatabase) {
    private val readNotificationIds = mutableSetOf<String>()
    private val completedNotificationIds = mutableSetOf<String>()
    private val deletedNotificationIds = mutableSetOf<String>()
    private val mutex = Mutex()

    fun storeNotification(
        key: String,
        channelId: String?,
        title: String?,
        body: String?,
        timestamp: Long,
        priority: Long = 1,
        read: Boolean = false,
        completed: Boolean = false,
        userFacing: Boolean = true,
        additionalData: Map<String, String>? = null
    ) {
        Scope.launch(Dispatchers.IO) {
            mutex.withLock {
                if (key !in deletedNotificationIds) {
                    storeNotification(
                        NotificationEntity.toEntity(
                            key,
                            channelId,
                            title,
                            body,
                            timestamp,
                            priority,
                            read,
                            completed,
                            userFacing,
                            additionalData
                        )
                    )
                } else {
                    deletedNotificationIds.remove(key)
                }
            }
        }
    }

    suspend fun storeNotification(notification: NotificationEntity) {
        mutex.withLock {
            Napier.i { "Delete Notification: $deletedNotificationIds. Notification to store: $notification" }
            if (notification.notificationId !in deletedNotificationIds) {
                val updatedNotification = notification.copy(
                    read = if (notification.notificationId in readNotificationIds) {
                        readNotificationIds.remove(notification.notificationId)
                        true
                    } else notification.read,
                    completed = if (notification.notificationId in completedNotificationIds) {
                        completedNotificationIds.remove(notification.notificationId)
                        true
                    } else notification.completed
                )
                appDatabase.notificationDao().insert(updatedNotification)
            } else {
                deletedNotificationIds.remove(notification.notificationId)
            }
        }
    }

    suspend fun storeNotifications(notifications: List<NotificationEntity>) {
        mutex.withLock {
            val (notificationsToStore, notificationsToDelete) = notifications.partition { it.notificationId !in deletedNotificationIds }
            Napier.i { "Delete Notification: $deletedNotificationIds. Storing notifications: $notificationsToStore. Notifications to delete: $notificationsToDelete" }

            val updatedNotifications = notificationsToStore.map { notification ->
                notification.copy(
                    read = notification.notificationId in readNotificationIds,
                    completed = notification.notificationId in completedNotificationIds
                )
            }
            appDatabase.notificationDao().insertAll(updatedNotifications)
            notificationsToDelete.forEach { deletedNotificationIds.remove(it.notificationId) }
        }
    }

    suspend fun getNotification(notificationId: String): NotificationEntity? {
        return appDatabase.notificationDao().getById(notificationId)
    }

    fun getAllUserFacingNotifications(): Flow<List<NotificationEntity>> {
        val dbFlow = appDatabase.notificationDao().getByPastUserFacingFlow(true)

        return combine(
            dbFlow,
            alignedNowFlow(periodMs = 30_000L)
        ) { list, now ->
            list.filter { it.timestamp != null && it.timestamp <= now }
        }.distinctUntilChanged()
    }

    suspend fun update(notificationId: String, read: Boolean? = null, priority: Long? = null) {
        mutex.withLock {
            val notification = appDatabase.notificationDao().getById(notificationId)
            notification?.let {
                val updatedNotification = it.copy(
                    read = read ?: it.read,
                    priority = priority ?: it.priority
                )
                appDatabase.notificationDao().update(updatedNotification)
            }
        }
    }

    fun setNotificationReadStatus(key: String, read: Boolean = true) {
        if (read) {
            readNotificationIds.add(key)
        } else {
            readNotificationIds.remove(key)
        }

        Scope.launch(Dispatchers.IO) {
            mutex.withLock {
                appDatabase.notificationDao().updateReadStatus(key, read)
                readNotificationIds.remove(key)
            }
        }
    }

    fun setNotificationCompletedStatus(key: String, completed: Boolean = true) {
        if (completed) {
            readNotificationIds.add(key)
            completedNotificationIds.add(key)
        } else {
            readNotificationIds.remove(key)
            completedNotificationIds.remove(key)
        }

        Scope.launch(Dispatchers.IO) {
            mutex.withLock {
                appDatabase.notificationDao().updateCompletedStatus(key, completed)
                if (completed) {
                    appDatabase.notificationDao().updateReadStatus(key, true)
                }
                completedNotificationIds.remove(key)
            }
        }
    }

    suspend fun scheduledNotificationCount(): Int {
        return appDatabase.notificationDao()
            .getScheduledNotificationCount(Clock.System.now().epochSeconds)
    }

    suspend fun scheduledNotifications(): List<NotificationEntity> {
        return appDatabase.notificationDao()
            .getScheduledNotifications(Clock.System.now().epochSeconds)
    }


    fun deleteNotification(notificationId: String) {
        Scope.launch(Dispatchers.IO) {
            deletedNotificationIds.add(notificationId)
            mutex.withLock {
                Napier.i { "Delete Notification: $deletedNotificationIds" }
                appDatabase.notificationDao().deleteById(notificationId)
                deletedNotificationIds.remove(notificationId)
                Napier.i { "Deleted Notification: $deletedNotificationIds" }
            }
        }
    }

    suspend fun deleteAll() {
        appDatabase.notificationDao().deleteAll()
    }
}