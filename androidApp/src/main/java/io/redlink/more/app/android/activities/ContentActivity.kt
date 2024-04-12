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
import androidx.compose.ui.platform.LocalContext
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen.Companion.NavigationNotificationIDKey
import io.redlink.more.app.android.activities.consent.ConsentView
import io.redlink.more.app.android.activities.login.LoginView
import io.redlink.more.app.android.extensions.applicationId
import io.redlink.more.app.android.extensions.stringResource
import io.redlink.more.app.android.shared_composables.AppVersion
import io.redlink.more.app.android.shared_composables.MoreBackground
import io.redlink.more.more_app_mutliplatform.services.notification.NotificationManager

class ContentActivity : ComponentActivity() {
    private val viewModel = ContentViewModel()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.getStringExtra(NotificationManager.DEEP_LINK)?.let {
            var deepLink = it
            intent.getStringExtra(NotificationManager.MSG_ID)?.let { msgId ->
                if (!deepLink.contains(NavigationNotificationIDKey)) {
                    deepLink += if (deepLink.contains("?")) {
                        "&$NavigationNotificationIDKey=$msgId"
                    } else {
                        "?$NavigationNotificationIDKey=$msgId"
                    }
                }
            }
            intent.putExtra(NotificationManager.DEEP_LINK, deepLink)
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
    if (viewModel.hasCredentials.value) {
        viewModel.openMainActivity(LocalContext.current)
    } else {
        MoreBackground(showBackButton = false, alertDialogModel = viewModel.alertDialogOpen.value) {
            if (viewModel.loginViewScreenNr.value == 0) {
                LoginView(model = viewModel.loginViewModel)
                AppVersion()
            } else {
                ConsentView(model = viewModel.consentViewModel)
            }
        }
    }
}
