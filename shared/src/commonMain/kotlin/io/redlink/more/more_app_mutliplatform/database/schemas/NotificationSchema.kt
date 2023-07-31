package io.redlink.more.more_app_mutliplatform.database.schemas

import io.realm.kotlin.ext.realmDictionaryOf
import io.realm.kotlin.ext.toRealmDictionary
import io.realm.kotlin.types.RealmDictionary
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import io.redlink.more.more_app_mutliplatform.extensions.toRealmInstant
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
    var notificationData: RealmDictionary<String> = realmDictionaryOf()

    companion object {
        fun toSchema(
            notificationId: String,
            channelId: String?,
            title: String?,
            notificationBody: String?,
            timestamp: Long,
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
                this.timestamp = Instant.fromEpochMilliseconds(timestamp).toRealmInstant()
                this.priority = priority
                this.read = read
                this.userFacing = userFacing
                this.notificationData = notificationData?.toRealmDictionary() ?: realmDictionaryOf()
            }
        }
    }
}