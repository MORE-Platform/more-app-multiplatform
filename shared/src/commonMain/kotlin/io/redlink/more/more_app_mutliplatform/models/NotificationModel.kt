package io.redlink.more.more_app_mutliplatform.models

import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.extensions.toInstant

data class NotificationModel(
    var notificationId: String,
    var channelId: String?,
    var title: String,
    var notificationBody: String,
    var timestamp: Long,
    var priority: Long,
    var read: Boolean,
    var userFacing: Boolean,
    var deepLink: String?,
    var notificationData: Map<String, String>
) {

    companion object {
        fun createModelsFrom(notifications: List<NotificationSchema?>): List<NotificationModel> {
            return notifications.mapNotNull {
                it?.let {
                    val channelId = it.channelId
                    val title = it.title ?: return@mapNotNull null
                    val notificationBody = it.notificationBody ?: return@mapNotNull null
                    val timestamp = it.timestamp ?: return@mapNotNull null
                    val notificationData = it.notificationData
                    NotificationModel(
                        notificationId = it.notificationId,
                        channelId = channelId,
                        title = title,
                        notificationBody = notificationBody,
                        timestamp = timestamp.toInstant().toEpochMilliseconds(),
                        priority = it.priority,
                        read = it.read,
                        userFacing = it.userFacing,
                        deepLink = it.deepLink,
                        notificationData = notificationData
                    )
                }
            }
        }
    }
}