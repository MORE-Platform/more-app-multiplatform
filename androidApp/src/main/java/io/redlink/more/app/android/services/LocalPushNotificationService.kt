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
import io.redlink.more.more_app_mutliplatform.viewModels.notifications.LocalNotificationListener

class LocalPushNotificationService(private val context: Context) : LocalNotificationListener {
    override fun displayNotification(notification: NotificationSchema) {
        notification.title?.let { title ->
            notification.notificationBody?.let { message ->
                val intent = Intent(context, ContentActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.action = NotificationBroadcastReceiver.NOTIFICATION_SET_ON_READ_ACTION
                intent.putExtra(NOTIFICATION_KEY, notification.notificationId)

                notification.deepLink?.let {
                    intent.data = Uri.parse(it)
                }
                val pendingIntent = PendingIntent.getActivity(
                    context, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )

                val channelId: String =
                    notification.channelId ?: context.getString(R.string.default_channel_id)
                val notificationBuilder = NotificationCompat.Builder(context, channelId)
                    .setSmallIcon(R.mipmap.ic_more_logo_hf_v2_round)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setAutoCancel(true)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setContentIntent(pendingIntent)

                context.getSystemService(NotificationManager::class.java)
                    ?.let { notificationManager ->
                        val name = context.getString(R.string.notification_channel_name)
                        val descriptionText =
                            context.getString(R.string.notification_channel_description)
                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                        val mChannel = NotificationChannel(channelId, name, importance)
                        mChannel.description = descriptionText
                        notificationManager.createNotificationChannel(mChannel)

                        notificationManager.notify(0, notificationBuilder.build())
                    }
            }
        }
    }

    override fun clearNotifications() {
        context.getSystemService(NotificationManager::class.java)?.let {
            it.cancelAll()
        }
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

    companion object {
        const val NOTIFICATION_KEY = "notification_key"
    }
}