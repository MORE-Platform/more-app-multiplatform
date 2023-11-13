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
package io.redlink.more.app.android.activities.setting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.shared_composables.Accordion
import io.redlink.more.app.android.shared_composables.BasicText
import io.redlink.more.app.android.shared_composables.SmallTextButton
import io.redlink.more.app.android.ui.theme.MoreColors
import io.redlink.more.app.android.ui.theme.moreImportant
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen


@Composable
fun SettingsView(
    model: SettingsViewModel,
    navController: NavController
) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.SETTINGS.routeWithParameters())
    LaunchedEffect(route) {
        model.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            model.viewDidDisappear()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 16.dp)
        ) {

            item {
                BasicText(
                    text = getStringResource(id = R.string.more_settings_permission_information),
                    color = MoreColors.TextDefault,
                )

                Spacer(Modifier.height(24.dp))

                /*
                model.permissionModel.value?.let {
                    Spacer(Modifier.height(24.dp))

                    Accordion(
                        title = getStringResource(id = R.string.more_study_consent),
                        description = it.studyConsentInfo,
                        hasCheck = true,
                        hasSmallTitle = true,
                        hasPreview = false
                    )
                }
                */


            }

            items(model.permissionModel.value?.consentInfo ?: emptyList()) { consentInfo ->
                Accordion(
                    title = consentInfo.title,
                    description = consentInfo.info,
                    hasCheck = true,
                    hasSmallTitle = true,
                    hasPreview = false
                )
            }
        }
    }
}