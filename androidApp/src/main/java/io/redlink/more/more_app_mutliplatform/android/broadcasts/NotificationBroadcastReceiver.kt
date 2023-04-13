package io.redlink.more.more_app_mutliplatform.android.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.redlink.more.more_app_mutliplatform.android.services.PushNotificationService
import io.redlink.more.more_app_mutliplatform.database.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private var notificationRepository = NotificationRepository()

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let { intent ->
            if (intent.action == NOTIFICATION_SET_ON_READ_ACTION) {
                intent.getStringExtra(PushNotificationService.NOTIFICATION_KEY)?.let { key ->
                    scope.launch {
                        notificationRepository.setNotificationReadStatus(key, true)
                    }
                }
            }
        }
    }

    companion object {
        const val NOTIFICATION_SET_ON_READ_ACTION = "io.redlink.more.app.android.NOTIFICATION_ACTION_READ"
    }
}