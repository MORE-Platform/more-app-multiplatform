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
        fun createModelsFrom(notifications: List<NotificationEntity?>): List<NotificationModel> {
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
                        timestamp = timestamp,
                        priority = it.priority,
                        read = it.read,
                        completed = it.completed,
                        userFacing = it.userFacing,
                        deepLink = it.deepLink(),
                        notificationData = it.getNotificationDataMap()
                    )
                }
            }
        }
    }
}