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

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import io.github.aakira.napier.Napier
import io.redlink.more.app.android.MoreApplication
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.consent.ConsentView
import io.redlink.more.app.android.activities.consent.Polar360ProfileFormView
import io.redlink.more.app.android.activities.login.LoginView
import io.redlink.more.app.android.activities.studyStates.StudyLoadingErrorView
import io.redlink.more.app.android.activities.studyStates.StudyLoadingView
import io.redlink.more.app.android.extensions.applicationId
import io.redlink.more.app.android.extensions.getSecureID
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.observations.Polar.Polar360UserProfile
import io.redlink.more.app.android.shared_composables.AppVersion
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.navigation.model.NavigationRouteParameter
import io.redlink.more.services.notification.NotificationManager
import io.redlink.more.viewModels.ViewManager
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class ContentActivity : ComponentActivity() {
    private val viewModel = ContentViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra(NotificationManager.DEEP_LINK)?.let {
            var deepLink = it
            Napier.d { "Received deep link: $deepLink" }
            intent.getStringExtra(NotificationManager.MSG_ID)?.let { msgId ->
                if (!deepLink.contains(NavigationRouteParameter.NOTIFICATION_ID.key)) {
                    deepLink += if (deepLink.contains("?")) {
                        "&${NavigationRouteParameter.NOTIFICATION_ID.key}=$msgId"
                    } else {
                        "?${NavigationRouteParameter.NOTIFICATION_ID.key}=$msgId"
                    }
                }
            }
            intent.putExtra(NotificationManager.DEEP_LINK, deepLink)
        }

        lifecycleScope.launch {
            combine(
                MoreApplication.shared!!.credentialRepository.hasCredentials,
                viewModel.registrationService.isLoading
            ) { hasCredentials, isLoading ->
                hasCredentials && !isLoading
            }.collect { shouldNavigateToMain ->
                if (shouldNavigateToMain) {
                    viewModel.openMainActivity(this@ContentActivity)
                }
            }
        }


        setContent {
            ContentView(viewModel = viewModel)
        }
    }

    companion object {
        val DEEPLINK = stringResource(R.string.app_scheme) + "://" + applicationId + "/"
    }
}

@Composable
fun ContentView(viewModel: ContentViewModel) {
    val hasCredentials by MoreApplication.shared!!.credentialRepository.hasCredentials.collectAsStateWithLifecycle()
    val credentialsLoaded by MoreApplication.shared!!.credentialRepository.credentialsLoaded.collectAsStateWithLifecycle()
    val studyLoadingError by ViewManager.studyLoadingError.collectAsStateWithLifecycle()
    val study by viewModel.registrationService.study.collectAsStateWithLifecycle()
    var showPolar360ProfileForm by remember { mutableStateOf(false) }

    val studyHasPolar360 = study?.observations
        ?.any { it.observationType.contains("polar360observation") } == true

    MoreBackground(showBackButton = false) {
        if (credentialsLoaded && !hasCredentials) {
            if (showPolar360ProfileForm) {
                Polar360ProfileFormView(onComplete = {
                    viewModel.registrationService.acceptConsent(
                        getSecureID(MoreApplication.appContext!!) ?: ""
                    )
                })
            } else if (study != null) {
                ConsentView(
                    registrationService = viewModel.registrationService,
                    onConsentAccepted = {
                        if (studyHasPolar360 && Polar360UserProfile.load() == null) {
                            showPolar360ProfileForm = true
                        } else {
                            viewModel.registrationService.acceptConsent(
                                getSecureID(MoreApplication.appContext!!) ?: ""
                            )
                        }
                    }
                )
            } else {
                LoginView(viewModel.registrationService)
                AppVersion()
            }
        } else if (studyLoadingError) {
            StudyLoadingErrorView()
        } else {
            StudyLoadingView()
        }
    }
}
