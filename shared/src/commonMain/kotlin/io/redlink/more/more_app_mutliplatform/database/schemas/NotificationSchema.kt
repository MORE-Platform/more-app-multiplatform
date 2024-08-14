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
package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.ext.toRealmDictionary
import io.realm.kotlin.types.RealmDictionary
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.getPlatform
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotification
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager
import io.redlink.more.more_app_mutliplatform.util.createUUID
import kotlinx.datetime.Instant

class NotificationSchema : RealmObject {
    @PrimaryKey
    var notificationId: String = ""
    var channelId: String? = ""
    var title: String? = ""
    var notificationBody: String? = ""
    var timestamp: RealmInstant? = RealmInstant.now()
    var priority: Long = 0
    var read: Boolean = false
    var userFacing: Boolean = true
    var deepLink: String? = null
    var notificationData: RealmDictionary<String> = realmDictionaryOf()

    override fun toString(): String {
        return "NotificationSchema(notificationId='$notificationId', channelId=$channelId, title=$title, notificationBody=$notificationBody, timestamp=${timestamp.toString()}, priority=$priority, read=$read, userFacing=$userFacing, deepLink=$deepLink, notificationData=$notificationData)"
    }

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

    companion object {
        fun build(title: String, notificationBody: String): NotificationSchema =
            NotificationSchema().apply {
                this.notificationId = createUUID()
                this.title = title
                this.notificationBody = notificationBody
                this.priority = if (getPlatform().name.contains("Android")) 2 else 1
            }

        fun toSchema(
            notificationId: String,
            channelId: String?,
            title: String?,
            notificationBody: String?,
            timestamp: Long? = null,
            priority: Long,
            read: Boolean,
            userFacing: Boolean,
            notificationData: Map<String, String>?,
            deepLink: String? = null
        ): NotificationSchema {
            return NotificationSchema().apply {
                this.notificationId = notificationId
                this.channelId = channelId
                this.title = title
                this.notificationBody = notificationBody
                this.read = read
                this.userFacing = userFacing
                this.notificationData =
                    notificationData?.mapKeys { it.key.replace(".", "_") }?.toRealmDictionary()
                        ?: realmDictionaryOf()
                this.deepLink = deepLink ?: extractDeepLink(this.notificationData)
                this.priority = if (this.deepLink != null) 2 else priority
                this.timestamp =
                    timestamp?.let { Instant.fromEpochMilliseconds(timestamp).toRealmInstant() }
                        ?: RealmInstant.now()
            }
        }

        fun toSchema(notification: PushNotification): NotificationSchema {
            return toSchema(
                notificationId = notification.msgId,
                channelId = null,
                title = notification.title,
                notificationBody = notification.body,
                timestamp = notification.timestamp?.toEpochMilliseconds(),
                priority = 1,
                read = false,
                userFacing = notification.type == "text",
                notificationData = notification.data?.mapValues { it.value.toString() },
                deepLink = notification.deepLink
            )
        }

        fun toSchemaList(notifications: List<PushNotification>): List<NotificationSchema> =
            notifications.map { toSchema(it) }

        private fun extractDeepLink(data: Map<String, String>) = data[NotificationManager.DEEP_LINK]
    }
}