/*
 * Copyright LBI-DHP and/or licensed to LBI-DHP under one or more
 * contributor license agreements (LBI-DHP: Ludwig Boltzmann Institute
 * for Digital Health and Prevention -- A research institute of the
 * Ludwig Boltzmann Gesellschaft, Österreichische Vereinigung zur
 * Förderung der wissenschaftlichen Forschung).
 * Licensed under the Apache 2.0 license (see https://www.apache.org/licenses/LICENSE-2.0).
 */
package io.redlink.more.models

import io.redlink.more.database.entities.NotificationEntity

data class NotificationModel(
    var notificationId: String,
    var channelId: String?,
    var title: String,
    var notificationBody: String,
    var timestamp: Long,
    var priority: Long,
    var read: Boolean,
    var completed: Boolean,
    var userFacing: Boolean,
    var deepLink: String?,
    var notificationData: Map<String, String>
) {

    companion object {
        fun createModelFrom(entity: NotificationEntity): NotificationModel? {
            val channelId = entity.channelId
            val title = entity.title ?: return null
            val notificationBody = entity.notificationBody ?: return null
            val timestamp = entity.timestamp ?: return null
            return NotificationModel(
                notificationId = entity.notificationId,
                channelId = channelId,
                title = title,
                notificationBody = notificationBody,
                timestamp = timestamp,
                priority = entity.priority,
                read = entity.read,
                completed = entity.completed,
                userFacing = entity.userFacing,
                deepLink = entity.deepLink(),
                notificationData = entity.getNotificationDataMap()
            )
        }

        fun createModelsFrom(notifications: List<NotificationEntity?>): List<NotificationModel> {
            return notifications.mapNotNull {
                it?.let { createModelFrom(it) }
            }
        }
    }
}