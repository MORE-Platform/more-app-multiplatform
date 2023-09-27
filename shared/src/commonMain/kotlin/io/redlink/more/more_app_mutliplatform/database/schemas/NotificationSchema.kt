package io.redlink.more.more_app_mutliplatform.database.schemas

import io.ktor.http.Url
import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.ext.toRealmDictionary
import io.realm.kotlin.types.RealmDictionary
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.asString
import io.redlink.more.more_app_mutliplatform.extensions.fromUTCtoCurrent
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.PushNotification
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.NotificationManager
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

    companion object {
        fun toSchema(
            notificationId: String,
            channelId: String?,
            title: String?,
            notificationBody: String?,
            timestamp: Long? = null,
            priority: Long,
            read: Boolean,
            userFacing: Boolean,
            notificationData: Map<String, String>?
        ): NotificationSchema {
            return NotificationSchema().apply {
                this.notificationId = notificationId
                this.channelId = channelId
                this.title = title
                this.notificationBody = notificationBody
                this.priority = priority
                this.read = read
                this.userFacing = userFacing
                this.notificationData =
                    notificationData?.mapKeys { it.key.replace(".", "_") }?.toRealmDictionary()
                        ?: realmDictionaryOf()
                this.deepLink = extractDeepLink(this.notificationData)
                this.timestamp = timestamp?.let { Instant.fromEpochMilliseconds(timestamp).toRealmInstant()  } ?: RealmInstant.now()
            }
        }

        fun toSchema(notification: PushNotification): NotificationSchema {
            return NotificationSchema().apply {
                this.notificationId = notification.msgId
                this.title = notification.title
                this.notificationBody = notification.body
                this.userFacing = notification.type == "text"
                this.priority = 2
                this.notificationData = notification.data?.entries?.associate {
                    it.key.replace(
                        ".",
                        "_"
                    ) to it.value.toString()
                }?.toRealmDictionary() ?: realmDictionaryOf()
                this.deepLink = extractDeepLink(this.notificationData)
                this.timestamp = notification.timestamp?.toRealmInstant() ?: RealmInstant.now()
            }
        }

        fun toSchemaList(notifications: List<PushNotification>): List<NotificationSchema> =
            notifications.map { toSchema(it) }

        private fun extractDeepLink(data: Map<String, String>) = data[NotificationManager.DEEP_LINK]
    }
}