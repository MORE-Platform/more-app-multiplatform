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
package io.redlink.more.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import io.redlink.more.getPlatform
import io.redlink.more.services.network.openapi.model.PushNotification
import io.redlink.more.services.notification.NotificationManager
import io.redlink.more.util.createUUID
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val notificationId: String = createUUID(),
    val channelId: String? = "",
    val title: String? = "",
    val notificationBody: String? = "",
    val timestamp: Long? = Clock.System.now().epochSeconds,
    val priority: Long = 0,
    val read: Boolean = false,
    val completed: Boolean = false,
    val userFacing: Boolean = true,
    val deepLink: String? = null,
    val notificationData: String = "{}" // JSON string representation
) {
    @Ignore
    fun timestampInstant() = timestamp?.let { Instant.fromEpochSeconds(it) }

    @Ignore
    fun getNotificationDataMap(): Map<String, String> {
        return try {
            Json.decodeFromString<Map<String, String>>(notificationData)
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @Ignore
    fun deepLink(): String? = deepLink?.let {
        if (!it.contains("notificationId=")) {
            if (it.contains("?")) {
                "$it&notificationId=$notificationId"
            } else {
                "$it?notificationId=$notificationId"
            }
        } else {
            it
        }
    }

    override fun toString(): String {
        return "NotificationEntity(notificationId='$notificationId', channelId=$channelId, title=$title, notificationBody=$notificationBody, timestamp=${timestamp.toString()}, priority=$priority, read=$read, userFacing=$userFacing, deepLink=$deepLink, notificationData=$notificationData)"
    }

    companion object {
        fun build(title: String, notificationBody: String): NotificationEntity =
            NotificationEntity(
                notificationId = createUUID(),
                title = title,
                notificationBody = notificationBody,
                priority = if (getPlatform().name.contains("Android")) 2 else 1
            )

        fun toEntity(
            notificationId: String,
            channelId: String?,
            title: String?,
            notificationBody: String?,
            timestamp: Long? = null,
            priority: Long,
            read: Boolean,
            completed: Boolean,
            userFacing: Boolean,
            notificationData: Map<String, String>?,
            deepLink: String? = null
        ): NotificationEntity {
            val dataJson = try {
                notificationData?.mapKeys { it.key.replace(".", "_") }?.let {
                    Json.encodeToString(it)
                } ?: "{}"
            } catch (e: Exception) {
                "{}"
            }

            val extractedDeepLink = deepLink ?: extractDeepLink(notificationData ?: emptyMap())
            val finalPriority = if (extractedDeepLink != null) 2 else priority
            val finalTimestamp = timestamp ?: Clock.System.now().epochSeconds

            return NotificationEntity(
                notificationId = notificationId,
                channelId = channelId,
                title = title,
                notificationBody = notificationBody,
                read = read,
                completed = completed,
                userFacing = userFacing,
                notificationData = dataJson,
                deepLink = extractedDeepLink,
                priority = finalPriority,
                timestamp = finalTimestamp
            )
        }

        fun toEntity(notification: PushNotification): NotificationEntity {
            return toEntity(
                notificationId = notification.msgId ?: createUUID(),
                channelId = null,
                title = notification.title,
                notificationBody = notification.body,
                timestamp = notification.timestamp?.epochSeconds,
                priority = 1,
                read = false,
                completed = false,
                userFacing = (notification.type ?: "text") == "text",
                notificationData = notification.data?.mapValues { it.value.toString() },
                deepLink = notification.deepLink
            )
        }

        fun toEntityList(notifications: List<PushNotification>): List<NotificationEntity> =
            notifications.map { toEntity(it) }

        private fun extractDeepLink(data: Map<String, String>) =
            data[NotificationManager.Companion.DEEP_LINK]
    }
}
