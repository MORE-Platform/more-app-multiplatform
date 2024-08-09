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
package io.redlink.more.more_app_mutliplatform.database.repository

import io.github.aakira.napier.Napier
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.services.network.NetworkService
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class NotificationRepository : Repository<NotificationSchema>() {
    private val readNotificationIds = mutableSetOf<String>()
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
        userFacing: Boolean = true,
        additionalData: Map<String, String>? = null
    ) {
        Scope.launch {
            mutex.withLock {
                if (key !in deletedNotificationIds) {
                    storeNotification(
                        NotificationSchema.toSchema(
                            key,
                            channelId,
                            title,
                            body,
                            timestamp,
                            priority,
                            read,
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

    fun storeNotification(notification: NotificationSchema) {
        Scope.launch {
            mutex.withLock {
                Napier.i { "Delete Notification: $deletedNotificationIds. Notification to store: $notification" }
                if (notification.notificationId !in deletedNotificationIds) {
                    if (notification.notificationId in readNotificationIds) {
                        notification.read = true
                        readNotificationIds.remove(notification.notificationId)
                    }
                    realmDatabase().store(setOf(notification), UpdatePolicy.ERROR)
                } else {
                    deletedNotificationIds.remove(notification.notificationId)
                }
            }
        }
    }

    fun storeNotifications(notifications: List<NotificationSchema>) {
        Scope.launch {
            mutex.withLock {
                val (notificationsToStore, notificationsToDelete) = notifications.partition { it.notificationId !in deletedNotificationIds }
                Napier.i { "Delete Notification: $deletedNotificationIds. Storing notifications: $notificationsToStore. Notifications to delete: $notificationsToDelete" }
                realmDatabase().store(notificationsToStore.map {
                    it.apply {
                        read = it.notificationId in readNotificationIds
                    }
                }, UpdatePolicy.ERROR)
                notificationsToDelete.forEach { deletedNotificationIds.remove(it.notificationId) }
            }
        }
    }

    override fun count(): Flow<Long> = realmDatabase().count<NotificationSchema>()

    fun getAllNotifications() = realmDatabase().query<NotificationSchema>()

    fun getAllUserFacingNotifications() =
        realmDatabase().query<NotificationSchema>("userFacing == true")

    fun getUnreadUserNotifications() =
        realmDatabase().query<NotificationSchema>("userFacing == true AND read == false")

    fun update(notificationId: String, read: Boolean? = false, priority: Long? = null) {
        Scope.launch {
            mutex.withLock {
                realm()?.write {
                    this.query<NotificationSchema>("notificationId == $0", notificationId).first()
                        .find()
                        ?.let {
                            if (read != null) {
                                it.read = read
                            }
                            if (priority != null) {
                                it.priority = priority
                            }
                        }
                }
            }
        }
    }

    fun downloadMissedNotifications(networkService: NetworkService) {
        Scope.launch {
            storeNotifications(NotificationSchema.toSchemaList(networkService.downloadMissedNotifications()))
        }
    }

    fun allUserFacingNotifications() {
        realmDatabase().query<NotificationSchema>("userFacing == true")
    }

    fun countUserFacingNotifications() {
        realm()?.query<NotificationSchema>("userFacing == true")?.count()
    }

    fun setNotificationReadStatus(key: String, read: Boolean = true) {
        if (read) {
            readNotificationIds.add(key)
        } else {
            readNotificationIds.remove(key)
        }

        Scope.launch {
            mutex.withLock {
                realm()?.write {
                    val notification =
                        this.query<NotificationSchema>("notificationId == $0", key).first().find()
                    notification?.let {
                        it.read = read
                        readNotificationIds.remove(key)
                    }
                }
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        Scope.launch {
            deletedNotificationIds.add(notificationId)
            mutex.withLock {
                Napier.i { "Delete Notification: $deletedNotificationIds" }
                realm()?.write {
                    val notification =
                        this.query<NotificationSchema>("notificationId == $0", notificationId)
                            .first().find()
                    notification?.let {
                        delete(notification)
                        deletedNotificationIds.remove(notificationId)
                        Napier.i { "Deleted Notification: $deletedNotificationIds" }
                    }
                }
            }
        }
    }

    fun deleteAll() {
        realmDatabase().deleteAll()
    }
}