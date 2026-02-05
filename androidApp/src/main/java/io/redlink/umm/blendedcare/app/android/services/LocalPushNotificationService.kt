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
package io.redlink.umm.blendedcare.app.android.services

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.github.aakira.napier.Napier
import io.redlink.umm.blendedcare.app.android.R
import io.redlink.umm.blendedcare.app.android.activities.ContentActivity
import io.redlink.umm.blendedcare.app.android.broadcasts.NotificationBroadcastReceiver
import io.redlink.umm.blendedcare.app.android.extensions.jvmLocalDateTimeFromMilliseconds
import io.redlink.umm.blendedcare.app.android.util.AlarmUtils
import io.redlink.umm.participant.database.entities.NotificationEntity
import io.redlink.umm.participant.models.localize
import io.redlink.umm.participant.services.notification.LocalNotificationListener
import io.redlink.umm.participant.services.notification.NotificationManager.Companion.MSG_ID

class LocalPushNotificationService(private val context: Context) : LocalNotificationListener {
    private val defaultChannelId = context.getString(R.string.default_channel_id)
    override fun displayNotification(notification: NotificationEntity, badgeCount: Int) {
        notification.title?.let { title ->
            notification.notificationBody?.let { messageKeyOrText ->
                // If the notification should fire in the future, schedule it.
                val nowMillis = System.currentTimeMillis()
                val triggerAtMillis = (notification.timestamp ?: 0L) * 1000L
                if (triggerAtMillis > nowMillis + 1000L) {
                    val channelId = notification.channelId ?: defaultChannelId

                    createNotificationIntent(notification, channelId)?.let { alarmIntent ->
                        AlarmUtils.addAlarm(
                            context,
                            alarmIntent,
                            notification.notificationId,
                            triggerAtMillis
                        )
                        Napier.i(tag = "LocalPushNotificationService::displayNotification") {
                            "Notification scheduled for ${triggerAtMillis.jvmLocalDateTimeFromMilliseconds()} with id ${notification.notificationId}"
                        }
                    }
                } else {
                    val intent = Intent(context, ContentActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        action = NotificationBroadcastReceiver.NOTIFICATION_SET_ON_READ_ACTION
                        putExtra(MSG_ID, notification.notificationId)
                        notification.deepLink()?.let { data = Uri.parse(it) }
                    }

                    val pendingIntent = PendingIntent.getActivity(
                        context, 0, intent,
                        PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                    )

                    val channelId = notification.channelId ?: defaultChannelId
                    val notificationBuilder = NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.mipmap.ic_more_logo_hf_v2_round)
                        .setContentTitle(title)
                        .setContentText(messageKeyOrText.localize())
                        .setAutoCancel(true)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setContentIntent(pendingIntent)
                        .setNumber(1)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_LIGHTS or NotificationCompat.DEFAULT_VIBRATE)
                        .setCategory(NotificationCompat.CATEGORY_REMINDER)

                    val notificationManager =
                        context.getSystemService(NotificationManager::class.java)
                    if (notificationManager != null) {
                        val channel = notificationManager.getNotificationChannel(channelId)
                        if (channel == null) {
                            val name = context.getString(R.string.notification_channel_name)
                            val descriptionText =
                                context.getString(R.string.notification_channel_description)
                            val importance = NotificationManager.IMPORTANCE_HIGH
                            val mChannel = NotificationChannel(channelId, name, importance).apply {
                                description = descriptionText
                                enableVibration(true)
                                setShowBadge(true)
                            }
                            notificationManager.createNotificationChannel(mChannel)
                        }

                        notificationManager.notify(
                            notification.notificationId.hashCode(),
                            notificationBuilder.build()
                        )
                        Napier.i { "Sent Notification to device" }
                    } else {
                        Napier.e(tag = "NotificationError") { "Notification Manager is null" }
                    }
                }
            } ?: run {
                Napier.e(tag = "NotificationError") { "Notification message is null" }
            }
        } ?: run {
            Napier.e(tag = "NotificationError") { "Notification title is null" }
        }
    }

    override fun clearScheduledNotifications(notifications: List<NotificationEntity>) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        if (alarmManager == null) {
            Napier.e(tag = "NotificationError") { "AlarmManager is null" }
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            alarmManager.cancelAll()
        } else {
            notifications.forEach { notification ->
                createNotificationIntent(
                    notification,
                    notification.channelId ?: defaultChannelId
                )?.let {
                    AlarmUtils.cancelAllAlarms(context, it)
                    Napier.i { "Cleared scheduled notifications for ID: ${notification.notificationId}" }
                }
            }
        }
    }

    override fun deleteNotificationFromSystem(notificationId: String) {
        context.getSystemService(NotificationManager::class.java)?.cancel(notificationId.hashCode())
    }

    override fun clearNotifications() {
        context.getSystemService(NotificationManager::class.java)?.cancelAll()
    }

    override fun createNewFCMToken(onCompletion: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Napier.e("Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            } else {
                Napier.i("FCM_Token: ${task.result}")
                onCompletion(task.result)
            }
        })
    }

    override fun deleteFCMToken() {
        FirebaseMessaging.getInstance().deleteToken()
    }


    override fun updateBadgeCount(count: Int) {
        if (count <= 0) {
            context.getSystemService(NotificationManager::class.java)?.cancelAll()
        }
    }

    private fun createNotificationIntent(
        notification: NotificationEntity,
        channelId: String
    ): Intent? {
        return notification.title?.let { title ->
            notification.notificationBody?.let { body ->
                Intent(context, NotificationBroadcastReceiver::class.java).apply {
                    action = NotificationBroadcastReceiver.SCHEDULED_NOTIFICATION_ACTION
                    putExtra(
                        NotificationBroadcastReceiver.EXTRA_NOTIFICATION_ID,
                        notification.notificationId
                    )
                    putExtra(NotificationBroadcastReceiver.EXTRA_CHANNEL_ID, channelId)
                    putExtra(NotificationBroadcastReceiver.EXTRA_TITLE, title)
                    putExtra(NotificationBroadcastReceiver.EXTRA_MESSAGE, body)
                    putExtra(
                        NotificationBroadcastReceiver.EXTRA_DEEP_LINK,
                        notification.deepLink()
                    )
                }
            }
        }
    }
}