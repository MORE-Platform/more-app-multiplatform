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
package io.redlink.more.app.android.activities

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.activities.main.MainActivity
import io.redlink.more.app.android.extensions.showNewActivityAndClearStack
import io.redlink.more.app.android.workers.ScheduleUpdateWorker
import io.redlink.more.dialog.AlertController
import io.redlink.more.dialog.AlertDialogModel
import io.redlink.more.registration.RegistrationService
import io.redlink.more.scopes.Scope
import io.redlink.more.services.notification.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ContentViewModel : ViewModel() {
    val registrationService: RegistrationService =
        RegistrationService(MoreApplication.shared!!)

    val hasCredentials = mutableStateOf(false)

    val alertDialogOpen = mutableStateOf<AlertDialogModel?>(null)

    init {
        NavigationScreen.createDeepLinksForAllRoutes()
        viewModelScope.launch(Dispatchers.IO) {
            MoreApplication.shared!!.credentialRepository.hasCredentials.collect {
                hasCredentials.value = it
            }
        }
        viewModelScope.launch(Dispatchers.Main.immediate) {
            AlertController.alertDialogModel.collect {
                withContext(Dispatchers.Main) {
                    alertDialogOpen.value = it
                }
            }
        }
    }

    suspend fun openMainActivity(context: Context) {
        (context as? Activity)?.let { activity ->
            schedulePeriodicWorker(activity)
            handleDeepLinkAndOpenMain(activity)
        }
    }

    private fun schedulePeriodicWorker(activity: Activity) {
        val workManager = WorkManager.getInstance(activity)
        val worker = PeriodicWorkRequestBuilder<ScheduleUpdateWorker>(15, TimeUnit.MINUTES).build()
        workManager.enqueueUniquePeriodicWork(
            ScheduleUpdateWorker.WORKER_TAG,
            ExistingPeriodicWorkPolicy.KEEP,
            worker
        )
    }

    private suspend fun handleDeepLinkAndOpenMain(activity: Activity) {
        val rawDeepLink =
            activity.intent.getStringExtra("deepLink") ?: activity.intent.data?.toString()
        Napier.d { "Attached deeplink: $rawDeepLink" }
        val notificationId = activity.intent.getStringExtra(NotificationManager.MSG_ID)
        val sharedInstance = MoreApplication.shared
            ?: throw IllegalStateException("MoreApplication.shared is not initialized")
        val modifiedDeepLink = rawDeepLink?.let { link ->
            sharedInstance.deeplinkManager
                .modifyDeepLink(link)
                .firstOrNull()
        } ?: notificationId?.let {
            sharedInstance.deeplinkManager.getNotificationViewDeepLink(
                it
            ).firstOrNull()
        }

        notificationId?.let {
            Scope.launch {
                sharedInstance.notificationManager.markNotificationAsRead(it)
            }
        }

        Napier.d { "Modified deeplink: $modifiedDeepLink" }

        withContext(Dispatchers.Main) {
            activity.intent.data = modifiedDeepLink?.route?.toUri()
            openMain(activity)
        }
    }

    private fun openMain(context: Context) {
        (context as? Activity)?.let {
            showNewActivityAndClearStack(
                it, MainActivity::class.java,
                forwardExtras = true,
                forwardDeepLink = true
            )
        }
    }
}