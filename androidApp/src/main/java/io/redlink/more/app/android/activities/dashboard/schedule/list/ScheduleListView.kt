package io.redlink.more.app.android.activities.dashboard.schedule.list

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.redlink.more.app.android.activities.NavigationScreen
import io.redlink.more.app.android.activities.dashboard.schedule.ScheduleViewModel
import io.redlink.more.app.android.shared_composables.ScheduleList


@Composable
fun ScheduleListView(
    navController: NavController,
    routeString: String,
    scheduleViewModel: ScheduleViewModel,
    showButton: Boolean
) {
    val backStackEntry = remember { navController.currentBackStackEntry }
    val route = backStackEntry?.arguments?.getString(routeString)
    LaunchedEffect(route) {
        scheduleViewModel.viewDidAppear()
    }
    DisposableEffect(route) {
        onDispose {
            scheduleViewModel.viewDidDisappear()
        }
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        if (scheduleViewModel.schedules.isNotEmpty()) {
            ScheduleList(
                navController = navController,
                viewModel = scheduleViewModel,
                showButton = showButton
            )
        }
    }
}