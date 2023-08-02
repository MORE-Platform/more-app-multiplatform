package io.redlink.more.app.android.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import java.util.UUID

/*
Service to handle push notifications and firebase connections
 */

class FCMService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        Napier.d( "Refreshed token: $token")
        MoreApplication.shared!!.notificationManager.newFCMToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        Napier.d( "From: ${message.from}")
        if (message.data.isNotEmpty() || message.notification != null) {
            Napier.d { message.toString() }
            MoreApplication.shared!!.notificationManager.storeAndHandleNotification(MoreApplication.shared!!, message.daoFromRemoteMessage())
        }
    }
}

fun RemoteMessage.daoFromRemoteMessage(): NotificationSchema {
    val notificationId = this.data["MSG_ID"] ?: UUID.randomUUID().toString()
    return NotificationSchema.toSchema(
        notificationId = notificationId,
        title = this.notification?.title,
        notificationBody = this.notification?.body,
        read = false,
        userFacing = this.notification != null,
        priority = this.priority.toLong(),
        notificationData = this.data,
        channelId = null
    )
}