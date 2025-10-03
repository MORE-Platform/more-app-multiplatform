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
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.main.MainActivity
import io.redlink.more.app.android.extensions.applicationId
import io.redlink.more.app.android.extensions.showNewActivityAndClearStack
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.workers.ScheduleUpdateWorker
import io.redlink.more.more_app_mutliplatform.AlertController
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import io.redlink.more.more_app_mutliplatform.registration.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ContentViewModel : ViewModel() {
    val registrationService: RegistrationService = RegistrationService(MoreApplication.shared!!)

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

    fun openMainActivity(context: Context) {
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

    private fun handleDeepLinkAndOpenMain(activity: Activity) {
        val rawDeepLink =
            activity.intent.getStringExtra("deepLink") ?: activity.intent.data?.toString()
        val notificationId = activity.intent.getStringExtra(NotificationManager.MSG_ID)

        viewModelScope.launch(Dispatchers.IO) {
            val modifiedDeepLink = rawDeepLink?.let { link ->
                val sharedInstance = MoreApplication.shared
                    ?: throw IllegalStateException("MoreApplication.shared is not initialized")
                sharedInstance.deeplinkManager
                    .modifyDeepLink(link, stringResource(R.string.app_scheme), applicationId)
                    .firstOrNull()
            }

            val finalUri = when {
                modifiedDeepLink != null -> {
                    notificationId?.let {
                        val sharedInstance = MoreApplication.shared
                            ?: throw IllegalStateException("MoreApplication.shared is not initialized")
                        sharedInstance.notificationManager.handleNotificationInteraction(
                            it, modifiedDeepLink
                        )
                    }
                    modifiedDeepLink.toUri()
                }

                notificationId != null -> {
                    (ContentActivity.DEEPLINK + NavigationScreen.NOTIFICATIONS.routeWithParameters()).toUri()
                }

                else -> null
            }

            Napier.d { finalUri.toString() }
            withContext(Dispatchers.Main) {
                activity.intent.data = finalUri
                openMain(activity)
            }
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