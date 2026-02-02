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
package io.redlink.umm.blendedcare.app.android.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.aakira.napier.Napier
import io.redlink.umm.blendedcare.app.android.BlendedCareApplication
import io.redlink.umm.blendedcare.app.android.util.AlarmUtils
import io.redlink.umm.participant.database.entities.NotificationEntity
import io.redlink.umm.participant.services.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob())

    override fun onReceive(context: Context, intent: Intent?) {
        Napier.d(tag = "NotificationBroadcastReceiver") { "onReceive: ${intent?.action}" }
        if (intent?.action == SCHEDULED_NOTIFICATION_ACTION) {
            val notificationId = intent.getStringExtra(EXTRA_NOTIFICATION_ID) ?: return
            val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID)
            val title = intent.getStringExtra(EXTRA_TITLE) ?: ""
            val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
            val deepLink = intent.getStringExtra(EXTRA_DEEP_LINK)

            Napier.i(tag = "NotificationBroadcastReceiver") { "Received scheduled notification: $notificationId" }

            val notification = NotificationEntity(
                notificationId,
                channelId,
                title,
                message,
                deepLink = deepLink
            )

            AlarmUtils.removeAlarmId(context, notificationId.hashCode())
            val pendingResult = goAsync()
            scope.launch {
                try {
                    BlendedCareApplication.shared?.notificationManager?.displayNotification(
                        notification
                    )
                    Napier.d(tag = "NotificationBroadcastReceiver") {
                        "Displayed scheduled notification ${notification.notificationId}"
                    }

                    Napier.d(tag = "NotificationBroadcastReceiver") { "Calling scheduleObservationReminder()" }
                    BlendedCareApplication.shared?.observationService?.scheduleObservationReminder()
                    Napier.d(tag = "NotificationBroadcastReceiver") { "scheduleObservationReminder() returned" }
                } catch (t: Throwable) {
                    Napier.e(tag = "NotificationBroadcastReceiver", throwable = t) {
                        "Failed handling scheduled notification ${notification.notificationId}"
                    }
                } finally {
                    pendingResult.finish()
                }
            }

            return
        }

        if (intent?.action == NOTIFICATION_SET_ON_READ_ACTION) {
            intent.getStringExtra(NotificationManager.MSG_ID)?.let { key ->
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        BlendedCareApplication.shared?.repositories?.notification
                            ?.setNotificationReadStatus(key, true)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }

    companion object {
        const val NOTIFICATION_SET_ON_READ_ACTION =
            "io.redlink.umm.blendedcare.app.android.NOTIFICATION_ACTION_READ"
        const val SCHEDULED_NOTIFICATION_ACTION =
            "io.redlink.umm.blendedcare.app.android.SCHEDULED_NOTIFICATION"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_CHANNEL_ID = "extra_channel_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_DEEP_LINK = "extra_deep_link"
    }
}