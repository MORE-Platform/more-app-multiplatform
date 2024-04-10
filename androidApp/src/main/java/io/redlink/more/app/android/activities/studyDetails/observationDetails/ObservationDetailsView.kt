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
package io.redlink.more.app.android.activities.studyDetails.observationDetails

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.extensions.jvmLocalDate
import io.redlink.more.app.android.extensions.jvmLocalDateTime
import io.redlink.more.app.android.extensions.toDate
import io.redlink.more.app.android.shared_composables.*
import io.redlink.more.app.android.ui.theme.MoreColors

@Composable
fun ObservationDetailsView(
    viewModel: ObservationDetailsViewModel,
    navController: NavController
) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.OBSERVATION_DETAILS.routeWithParameters())
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
    LazyColumn(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ){
                HeaderTitle(
                    title = viewModel.observationDetailsModel.value.observationTitle,
                    modifier = Modifier
                        .weight(0.65f)
                        .padding(vertical = 11.dp)
                )
            }
            BasicText(
                text = viewModel.observationDetailsModel.value.observationType,
                color = MoreColors.Secondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            )

            TimeframeDays(
                viewModel.observationDetailsModel.value.start.jvmLocalDate(),
                viewModel.observationDetailsModel.value.end.jvmLocalDate(),
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
            TimeframeHours(
                viewModel.observationDetailsModel.value.start.jvmLocalDateTime(),
                viewModel.observationDetailsModel.value.end.jvmLocalDateTime(),
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Accordion(
                title = getStringResource(id = R.string.participant_information),
                description = viewModel.observationDetailsModel.value.participantInformation,
                hasCheck = false,
                hasPreview = false,
                isOpen = true
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}