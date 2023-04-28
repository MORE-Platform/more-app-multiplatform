package io.redlink.more.more_app_mutliplatform.database.repository

import io.realm.kotlin.ext.query
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import kotlinx.coroutines.flow.Flow

class NotificationRepository : Repository<NotificationSchema>() {
    fun storeNotification(
        key: String,
        channelId: String?,
        title: String?,
        body: String?,
        timestamp: Long,
        priority: Long,
        read: Boolean = false,
        userFacing: Boolean = true,
        additionalData: Map<String, String>? = null
    ) {
        realmDatabase.store(
            setOf(
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
        )
    }

    fun storeNotification(remoteMessage: NotificationSchema) {
        realmDatabase.store(setOf(remoteMessage))
    }

    override fun count(): Flow<Long> = realmDatabase.count<NotificationSchema>()

    fun getAllNotifications() = realmDatabase.query<NotificationSchema>()

    fun update(notificationId: String, read: Boolean? = false, priority: Long? = null) {
        realmDatabase.realm?.writeBlocking {
            this.query<NotificationSchema>("notificationId = $0", notificationId).first().find()
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

    fun allUserFacingNotifications() {
        realmDatabase.query<NotificationSchema>("userFacing == true")
    }

    fun countUserFacingNotifications() {
        realmDatabase.realm?.query<NotificationSchema>("userFacing == true")?.count()
    }

    fun setNotificationReadStatus(key: String, read: Boolean = true) {
        realmDatabase.realm?.writeBlocking {
            this.query<NotificationSchema>("notificationId = $0", key).first().find()?.let {
                it.read = read
            }
        }
    }

    fun deleteAll() {
        realmDatabase.deleteAll()
    }
}