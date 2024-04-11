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
package io.redlink.more.app.android.broadcasts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class NotificationBroadcastReceiver : BroadcastReceiver() {
    private var notificationRepository = MoreApplication.shared!!.notificationManager.notificationRepository

    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let { intent ->
            if (intent.action == NOTIFICATION_SET_ON_READ_ACTION) {
                intent.getStringExtra(NotificationManager.MSG_ID)?.let { key ->
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