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
package io.redlink.more.app.android.activities.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.dashboard.schedule.list.ScheduleListView
import io.redlink.more.app.android.extensions.getStringResource
import io.redlink.more.app.android.R
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.taskCompletion.TaskCompletionBarViewModel
import io.redlink.more.app.android.shared_composables.ScheduleListHeader


@Composable
fun DashboardView(navController: NavController, viewModel: DashboardViewModel, taskCompletionBarViewModel: TaskCompletionBarViewModel) {
//    PolarHeartRateObservation.scanForDevices()
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(NavigationScreen.DASHBOARD.routeWithParameters())
    LaunchedEffect(route) {
        viewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            viewModel.viewDidDisappear()
        }
    }
    Column(
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
    ){
        ScheduleListHeader(
            viewModel = viewModel.scheduleViewModel,
            navController = navController,
            taskCompletionBarViewModel = taskCompletionBarViewModel
        )
        Spacer(modifier = Modifier.height(10.dp))
        Column {
            if (!viewModel.studyActive.value) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.8f)
                        .padding(top = 16.dp)
                ){
                    Text(text = getStringResource(id = R.string.more_dashboard_study_not_active))
                }
            } else {
                ScheduleListView(navController, NavigationScreen.DASHBOARD.routeWithParameters(), scheduleViewModel = viewModel.scheduleViewModel, showButton = true)
            }
        }
    }
}
