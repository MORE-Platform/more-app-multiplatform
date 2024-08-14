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
package io.redlink.more.app.android.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.ContentActivity
import io.redlink.more.app.android.broadcasts.NotificationBroadcastReceiver
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import io.redlink.more.more_app_mutliplatform.services.notification.LocalNotificationListener
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager.Companion.MSG_ID

class LocalPushNotificationService(private val context: Context) : LocalNotificationListener {
    private val defaultChannelId = context.getString(R.string.default_channel_id)
    private val unreadChannelId = context.getString(R.string.unread_channel_id)
    private val unreadNotificationId = 1
    override fun displayNotification(notification: NotificationSchema) {
        notification.title?.let { title ->
            notification.notificationBody?.let { message ->
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

                val channelId =
                    notification.channelId ?: defaultChannelId
                val notificationBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_more_logo_hf_v2_round)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(pendingIntent)

                val notificationManager = context.getSystemService(NotificationManager::class.java)
                if (notificationManager != null) {
                    val channel = notificationManager.getNotificationChannel(channelId)
                    if (channel == null) {
                        val name = context.getString(R.string.notification_channel_name)
                        val descriptionText =
                            context.getString(R.string.notification_channel_description)
                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                        val mChannel = NotificationChannel(channelId, name, importance).apply {
                            description = descriptionText
                        }
                        notificationManager.createNotificationChannel(mChannel)
                    }

                    notificationManager.notify(
                        notification.notificationId.hashCode(),
                        notificationBuilder.build()
                    )
                } else {
                    Napier.e(tag = "NotificationError") { "Notification Manager is null" }
                }
            } ?: run {
                Napier.e(tag = "NotificationError") { "Notification message is null" }
            }
        } ?: run {
            Napier.e(tag = "NotificationError") { "Notification title is null" }
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
        if (count > 0) {
            val intent = Intent(context, ContentActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
            )
            val title = context.getString(R.string.notification_unread_title, count)
            val message = context.getString(R.string.notification_unread_content, count)
            val notificationBuilder = NotificationCompat.Builder(context, unreadChannelId)
                .setSmallIcon(R.mipmap.ic_more_logo_hf_v2_round)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(false)
                .setNumber(count)
                .setVibrate(longArrayOf(0))
                .setSound(null)
                .setContentIntent(pendingIntent)

            context.getSystemService(NotificationManager::class.java)?.let { notificationManager ->
                val channel = notificationManager.getNotificationChannel(unreadChannelId)
                if (channel == null) {
                    val name = context.getString(R.string.unread_channel_id)
                    val descriptionText =
                        context.getString(R.string.notification_channel_description)
                    val importance = NotificationManager.IMPORTANCE_LOW
                    val mChannel = NotificationChannel(unreadChannelId, name, importance).apply {
                        description = descriptionText
                    }
                    notificationManager.createNotificationChannel(mChannel)
                }
                notificationManager.notify(
                    unreadNotificationId,
                    notificationBuilder.build()
                )
            }
        } else {
            context.getSystemService(NotificationManager::class.java)?.cancel(unreadNotificationId)
        }
    }

    companion object {
        const val NOTIFICATION_KEY = "notification_key"
    }
}