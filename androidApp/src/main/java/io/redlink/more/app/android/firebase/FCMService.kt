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
        Napier.i( "Refreshed token: $token", tag = "FCMService::onNewToken")
        MoreApplication.shared!!.notificationManager.newFCMToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data.isNotEmpty() || message.notification != null) {
            Napier.i(tag = "FCMService::onMessageReceived") { message.daoFromRemoteMessage().toString()}
            MoreApplication.shared!!.notificationManager.storeAndHandleNotification(MoreApplication.shared!!, message.daoFromRemoteMessage(), true)
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
        priority = 1,
        notificationData = this.data,
        channelId = null,
        timestamp = sentTime
    )
}
