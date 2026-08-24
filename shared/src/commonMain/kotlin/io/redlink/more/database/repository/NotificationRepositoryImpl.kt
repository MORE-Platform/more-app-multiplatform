/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.database.repository

import io.github.aakira.napier.Napier
import io.redlink.more.database.AppDatabase
import io.redlink.more.database.entities.NotificationEntity
import io.redlink.more.scopes.Scope
import io.redlink.more.util.alignedNowFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock

class NotificationRepositoryImpl(private val appDatabase: AppDatabase) : NotificationRepository {
    private val readNotificationIds = mutableSetOf<String>()
    private val completedNotificationIds = mutableSetOf<String>()
    private val deletedNotificationIds = mutableSetOf<String>()
    private val mutex = Mutex()

    override suspend fun storeNotification(notification: NotificationEntity) {
        mutex.withLock {
            val notificationId = notification.notificationId
            if (notificationId !in deletedNotificationIds) {
                Napier.i { "Storing notification: $notification" }
                appDatabase.notificationDao().insert(notification.copyAndModify())
            } else {
                Napier.i { "Notification marked for deletion: $notification" }
                deletedNotificationIds.remove(notificationId)
            }
        }
    }

    override suspend fun storeNotifications(notifications: List<NotificationEntity>) {
        mutex.withLock {
            val (notificationsToStore, notificationsToDelete) = notifications.partition { it.notificationId !in deletedNotificationIds }
            Napier.i { "Storing ${notificationsToStore.size} notifications and deleting ${notificationsToDelete.size} notifications" }

            appDatabase.notificationDao().insertAll(notificationsToStore.map { it.copyAndModify() })
            notificationsToDelete.forEach { deletedNotificationIds.remove(it.notificationId) }
        }
    }

    override suspend fun getNotification(notificationId: String): NotificationEntity? {
        return appDatabase.notificationDao().getById(notificationId)
    }

    override fun getAllUserFacingNotifications(): Flow<List<NotificationEntity>> {
        val dbFlow = appDatabase.notificationDao().getByPastUserFacingFlow(true)

        return combine(
            dbFlow,
            alignedNowFlow(periodMs = 30_000L)
        ) { list, now ->
            list.filter { it.timestamp != null && it.timestamp <= now }
        }.distinctUntilChanged()
    }

    override suspend fun update(notificationId: String, read: Boolean?, priority: Long?) {
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

    override fun setNotificationReadStatus(key: String, read: Boolean) {
        if (read) {
            readNotificationIds.add(key)
        } else {
            readNotificationIds.remove(key)
        }

        Scope.launch {
            mutex.withLock {
                val notification = appDatabase.notificationDao().getById(key)
                if (notification != null) {
                    appDatabase.notificationDao().updateReadStatus(key, read)
                }
                readNotificationIds.remove(key)
            }
        }
    }

    override fun setNotificationCompletedStatus(key: String, completed: Boolean) {
        if (completed) {
            readNotificationIds.add(key)
            completedNotificationIds.add(key)
        } else {
            readNotificationIds.remove(key)
            completedNotificationIds.remove(key)
        }

        Scope.launch {
            mutex.withLock {
                val notification = appDatabase.notificationDao().getById(key)
                if (notification != null) {
                    appDatabase.notificationDao().updateCompletedStatus(key, completed)
                    if (completed) {
                        appDatabase.notificationDao().updateReadStatus(key, true)
                    }
                }
                completedNotificationIds.remove(key)
            }
        }
    }

    override suspend fun scheduledNotificationCount(): Int {
        return appDatabase.notificationDao()
            .getScheduledNotificationCount(Clock.System.now().epochSeconds)
    }

    override suspend fun scheduledNotifications(): List<NotificationEntity> {
        return appDatabase.notificationDao()
            .getScheduledNotifications(Clock.System.now().epochSeconds)
    }

    override fun deleteNotification(notificationId: String) {
        Scope.launch {
            deletedNotificationIds.add(notificationId)
            mutex.withLock {
                Napier.i { "Delete Notification: $deletedNotificationIds" }
                appDatabase.notificationDao().deleteById(notificationId)
                deletedNotificationIds.remove(notificationId)
                Napier.i { "Deleted Notification: $deletedNotificationIds" }
            }
        }
    }

    override suspend fun deleteAll() {
        appDatabase.notificationDao().deleteAll()
    }

    private fun NotificationEntity.copyAndModify(): NotificationEntity {
        val updatedNotification = copy(
            read = if (notificationId in readNotificationIds) true else read,
            completed = if (notificationId in completedNotificationIds) true else completed
        )
        readNotificationIds.remove(notificationId)
        completedNotificationIds.remove(notificationId)
        return updatedNotification
    }

}
