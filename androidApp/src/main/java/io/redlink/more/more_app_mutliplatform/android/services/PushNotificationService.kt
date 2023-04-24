package io.redlink.more.more_app_mutliplatform.android.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import io.redlink.more.more_app_mutliplatform.android.R
import io.redlink.more.more_app_mutliplatform.android.activities.main.MainActivity
import io.redlink.more.more_app_mutliplatform.android.broadcasts.NotificationBroadcastReceiver.Companion.NOTIFICATION_SET_ON_READ_ACTION
import io.redlink.more.more_app_mutliplatform.android.extensions.getSystemService
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import io.redlink.more.more_app_mutliplatform.database.schemas.NotificationSchema
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset

import java.util.*

class PushNotificationService : Service() {
    private var notificationRepository = NotificationRepository()
    private val scope = CoroutineScope(Job() + Dispatchers.IO)
    private var notificationNumber = 0

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { intent ->
            notificationNumber++
            val title = intent.getStringExtra(NOTIFICATION_TITLE) ?: return@let
            val message = intent.getStringExtra(NOTIFICATION_MESSAGE) ?: return@let
            val channelId = intent.getStringExtra(NOTIFICATION_CHANNEL_ID)
            val shouldSend = intent.getBooleanExtra(NOTIFICATION_SHOULD_BE_SENT, true)
            val shouldStore = intent.getBooleanExtra(NOTIFICATION_SHOULD_BE_STORED, true)
            val priority = intent.getLongExtra(NOTIFICATION_PRIORITY, 1)
            val uniqueKey = UUID.randomUUID().toString()
            if (shouldSend) {
                sendNotification(uniqueKey, title, message, channelId)
            }
            if (shouldStore) {
                storeNotification(uniqueKey, title, message, channelId, priority)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun sendNotification(
        key: String,
        title: String,
        message: String,
        possibleChannelId: String?
    ) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.action = NOTIFICATION_SET_ON_READ_ACTION
        intent.putExtra(NOTIFICATION_KEY, key)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId: String = possibleChannelId ?: getString(R.string.default_channel_id)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_more_logo_hf_v2_round)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
            .setContentIntent(pendingIntent)

        getSystemService(NotificationManager::class.java)?.let { notificationManager ->
            val name = getString(R.string.notification_channel_name)
            val descriptionText = getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(channelId, name, importance)
            mChannel.description = descriptionText
            notificationManager.createNotificationChannel(mChannel)

            notificationManager.notify(0, notificationBuilder.build())
        }
    }

    private fun storeNotification(
        key: String,
        title: String,
        message: String,
        channelId: String?,
        priority: Long?
    ) {
        scope.launch {
            notificationRepository.storeNotification(
                key = key,
                title = title,
                body = message,
                channelId = channelId,
                priority = priority ?: 1,
                timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli()
            )
            if (--notificationNumber == 0) {
                stopSelf()
            }
        }
    }


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    companion object {
        private const val NOTIFICATION_TITLE = "notification_title"
        private const val NOTIFICATION_MESSAGE = "notification_message"
        private const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
        private const val NOTIFICATION_SHOULD_BE_SENT = "notification_should_be_sent"
        const val NOTIFICATION_KEY = "notification_key"
        const val NOTIFICATION_PRIORITY = "notification_priority"
        const val NOTIFICATION_SHOULD_BE_STORED = "notification_should_be_stored"

        fun sendNotification(
            context: Context,
            title: String,
            message: String,
            channelId: String? = null,
            shouldBeSent: Boolean = true,
            priority: Long? = 1,
            shouldBeStored: Boolean
        ) {
            val serviceIntent = Intent(context, PushNotificationService::class.java)
            serviceIntent.putExtra(NOTIFICATION_TITLE, title)
            serviceIntent.putExtra(NOTIFICATION_MESSAGE, message)
            serviceIntent.putExtra(NOTIFICATION_CHANNEL_ID, channelId)
            serviceIntent.putExtra(NOTIFICATION_SHOULD_BE_SENT, shouldBeSent)
            serviceIntent.putExtra(NOTIFICATION_PRIORITY, priority)
            serviceIntent.putExtra(NOTIFICATION_SHOULD_BE_STORED, shouldBeStored)
            context.startService(serviceIntent)
        }

        fun cancel(
            notificationIds: List<Int>
        ) {
            getSystemService(NotificationManager::class.java)?.let { notificationManager ->
                notificationIds.forEach { notificationManager.cancel(it) }
            }
        }

        fun daoFromRemoteMessage(remoteMessage: RemoteMessage): NotificationSchema {
            return NotificationSchema.toSchema(
                notificationId = remoteMessage.messageId ?: UUID.randomUUID().toString(),
                title = remoteMessage.notification?.title,
                notificationBody = remoteMessage.notification?.body,
                timestamp = LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli(),
                read = false,
                userFacing = remoteMessage.notification != null,
                priority = remoteMessage.priority.toLong(),
                notificationData = if (remoteMessage.data.isEmpty()) null else remoteMessage.data,
                channelId = null
            )
        }
    }
}
