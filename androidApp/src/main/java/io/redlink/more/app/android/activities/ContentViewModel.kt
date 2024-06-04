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
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.consent.ConsentViewModel
import io.redlink.more.app.android.activities.consent.ConsentViewModelListener
import io.redlink.more.app.android.activities.login.LoginViewModel
import io.redlink.more.app.android.activities.login.LoginViewModelListener
import io.redlink.more.app.android.activities.main.MainActivity
import io.redlink.more.app.android.extensions.applicationId
import io.redlink.more.app.android.extensions.showNewActivityAndClearStack
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.workers.ScheduleUpdateWorker
import io.redlink.more.more_app_mutliplatform.AlertController
import io.redlink.more.more_app_mutliplatform.models.AlertDialogModel
import io.redlink.more.more_app_mutliplatform.services.network.RegistrationService
import io.redlink.more.more_app_mutliplatform.services.network.openapi.model.Study
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager
import io.redlink.more.more_app_mutliplatform.util.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class ContentViewModel : ViewModel(), LoginViewModelListener, ConsentViewModelListener {
    private val registrationService: RegistrationService by lazy {
        RegistrationService(
            MoreApplication.shared!!
        )
    }

    val loginViewModel: LoginViewModel by lazy { LoginViewModel(registrationService, this) }
    val consentViewModel: ConsentViewModel by lazy { ConsentViewModel(registrationService, this) }

    val hasCredentials =
        mutableStateOf(MoreApplication.shared!!.credentialRepository.hasCredentials())
    val loginViewScreenNr = mutableStateOf(0)

    val alertDialogOpen = mutableStateOf<AlertDialogModel?>(null)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            AlertController.alertDialogModel.collect {
                withContext(Dispatchers.Main) {
                    alertDialogOpen.value = it
                }
            }
        }
    }

    fun openMainActivity(context: Context) {
        (context as? Activity)?.let {
            val workManager = WorkManager.getInstance(context)
            val worker =
                PeriodicWorkRequestBuilder<ScheduleUpdateWorker>(15L, TimeUnit.MINUTES).build()
            workManager.enqueueUniquePeriodicWork(
                ScheduleUpdateWorker.WORKER_TAG,
                ExistingPeriodicWorkPolicy.KEEP,
                worker
            )

            (it.intent.getStringExtra("deepLink") ?: it.intent.data?.toString())?.let { deepLink ->
                Scope.launch {
                    MoreApplication.shared!!.deeplinkManager.modifyDeepLink(
                        deepLink, stringResource(R.string.app_scheme), applicationId
                    ).firstOrNull()?.let { modifiedDeepLink ->
                        val link = Uri.parse(modifiedDeepLink)
                        it.intent.getStringExtra(NotificationManager.MSG_ID)
                            ?.let { notificationId ->
                                MoreApplication.shared!!.notificationManager.handleNotificationInteraction(
                                    notificationId,
                                    modifiedDeepLink
                                )
                            }
                        withContext(Dispatchers.Main) {
                            it.intent.data = link
                            openMain(it)
                        }

                    } ?: run {
                        withContext(Dispatchers.Main) {
                            it.intent.getStringExtra(NotificationManager.MSG_ID)?.let { msgId ->
                                val route =
                                    ContentActivity.DEEPLINK + NavigationScreen.NOTIFICATIONS.routeWithParameters();
                                val link = Uri.parse(route)
                                it.intent.data = link
                            }
                            openMain(it)
                        }
                    }
                }
            } ?: run {
                it.intent.getStringExtra(NotificationManager.MSG_ID)?.let { msgId ->
                    val route =
                        ContentActivity.DEEPLINK + NavigationScreen.NOTIFICATIONS.routeWithParameters();
                    val link = Uri.parse(route)
                    it.intent.data = link
                }
                openMain(it)
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

    private fun showLoginView() {
        viewModelScope.launch(Dispatchers.Main) {
            loginViewScreenNr.value = 0
            registrationService.reset()
        }
    }

    private fun showConsentView() {
        viewModelScope.launch(Dispatchers.Main) {
            loginViewScreenNr.value = 1
        }
    }

    override fun tokenIsValid(study: Study) {
        this.consentViewModel.setConsentInfo(study.consentInfo)
        this.consentViewModel.buildConsentModel()
        showConsentView()
    }

    override fun credentialsStored() {
        viewModelScope.launch(Dispatchers.Main) {
            hasCredentials.value = true
        }
    }

    override fun decline() {
        showLoginView()
    }
}